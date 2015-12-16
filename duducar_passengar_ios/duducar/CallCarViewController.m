//
//  CallCarViewController.m
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//
//叫车页面
#import "CallCarViewController.h"
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import "Driver.h"
#import "StartEndView.h"
#import "OrderInfoView.h"
#import "CostEstimationViewController.h"
#import "PaymentViewController.h"
@interface CallCarViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,UIAlertViewDelegate>
{
    BMKLocationService *_locService;
    BMKMapView * _mapView;
    
    StartEndView * startEndView;
    
    OrderInfoView * driverInfoView;

    BOOL isDown; //是否是向下滑
    
    NSString *orderStatus;
}
@end

static NSString * responseNotificationName = @"DDSocketResponseNotification";

@implementation CallCarViewController
- (void)viewDidLoad {
    [super viewDidLoad];

    /*
     * Init UI
     */
    UIBarButtonItem * leftItem = [[UIBarButtonItem alloc]initWithTitle:@"返回" style:UIBarButtonItemStyleDone target:self action:@selector(back:)];
    self.navigationItem.leftBarButtonItem = leftItem;
    
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _mapView.zoomLevel = 15;
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    
    startEndView = [[StartEndView alloc]initWithFrame:CGRectMake(20, 80, self.view.frame.size.width-40, 100)];
    [self.view addSubview:startEndView];
    startEndView.startLabel.text = _startLocation.name;
    startEndView.endLabel.text = _endLocation.name;
    
    driverInfoView= [[OrderInfoView alloc]initWithFrame:CGRectMake(0, self.view.frame.size.height, self.view.frame.size.width, 150)];
    driverInfoView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:driverInfoView];
    [driverInfoView smallViewFrame];
    
    UISwipeGestureRecognizer * swipeUp = [[UISwipeGestureRecognizer alloc]initWithTarget:self action:@selector(swipe:)];
    swipeUp.direction = UISwipeGestureRecognizerDirectionUp;
    swipeUp.numberOfTouchesRequired =1;
    [driverInfoView addGestureRecognizer:swipeUp];
    
    UISwipeGestureRecognizer * swipDown = [[UISwipeGestureRecognizer alloc]initWithTarget:self action:@selector(swipe:)];
    swipDown.direction = UISwipeGestureRecognizerDirectionDown;
    swipDown.numberOfTouchesRequired =1;
    [driverInfoView addGestureRecognizer:swipDown];
    
    /*
     * Start Location Service
     */
    _locService = [[BMKLocationService alloc]init];
    [_locService startUserLocationService];
    
    
    /*
     * 监听来自Socket的服务消息
     */
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
    
    
    /*
     * 处理当前活跃订单
     */
    if(self.activeOrder!= nil)
    {
        NSString *driverString = [self.activeOrder objectForKey:@"driver"];
        //Deserialastion a Json String into Dictionary
        NSError *jsonError;
        NSData  *objectData = [driverString dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *driverDict = [NSJSONSerialization JSONObjectWithData:objectData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&jsonError];
        self.orderDriver = [[Driver alloc]initWithDic:driverDict];
        
        _startLocation = [[Location alloc]init];
        _startLocation.name = [self.activeOrder objectForKey:@"start"];
        double start_lat = [[self.activeOrder objectForKey:@"start_lat"] doubleValue];
        double start_lng =[[self.activeOrder objectForKey:@"start_lng"] doubleValue];
        [_startLocation setCoordinate2D:CLLocationCoordinate2DMake(start_lat, start_lng)];

        
        _endLocation = [[Location alloc]init];
        _endLocation.name = [self.activeOrder objectForKey:@"destination"];
        double end_lat = [[self.activeOrder objectForKey:@"destination_lat"] doubleValue];
        double end_lng =[[self.activeOrder objectForKey:@"destination_lng"] doubleValue];
        [_endLocation setCoordinate2D:CLLocationCoordinate2DMake(end_lat, end_lng)];
        
        startEndView.startLabel.text = _startLocation.name;
        startEndView.endLabel.text = _endLocation.name;
        
        //出现底部的司机界面
        driverInfoView.driver = self.orderDriver;
        [UIView animateWithDuration:.3 animations:^{
            driverInfoView.frame = CGRectMake(0, self.view.frame.size.height-150, self.view.frame.size.width, 150);
            isDown = YES;
            [driverInfoView smallViewFrame];
        }];
        
        //处理订单状态 '1-订单初始化 2-接单 3-开始 4-结束 5-取消’,
        orderStatus = [self.activeOrder objectForKey:@"order_status"];
        
    }
    else if(_startLocation!=nil && _endLocation!=nil)
    {
        [self callCar];
    }

}

- (void)dealloc {
    if (_mapView) {
        _mapView = nil;
    }
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:@"DDSocketResponseNotification" object:nil];
    _locService.delegate =self;
    _mapView.delegate = self;
    [_mapView viewWillAppear];
}
-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    _locService.delegate = nil;
    _locService.delegate =nil;
    [_mapView viewWillDisappear];
}

#pragma mark -==== 取消订单=====
-(void)cancelOrder
{
    NSDictionary *param;
    if([orderStatus isEqualToString:@"1"])
    {
        param = @ {@"cmd": @"cancel_order", @"role": @"2"};
    } else {
        param = @ {@"cmd": @"cancel_order", @"reason_id":@"1", @"role": @"2"};
    }
    
    [[DDSocket currentSocket] sendRequest:param];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex ==0 )
    {
        NSLog(@"取消");
    }
    else if(buttonIndex == 1)
    {
        //确定
        [self cancelOrder];
    }
}

-(void)swipe:(UISwipeGestureRecognizer *)tap
{
    if(tap.direction == UISwipeGestureRecognizerDirectionDown)
    {
        if(isDown==NO)
        {
            [UIView animateWithDuration:0.3 animations:^{
                driverInfoView.frame = CGRectMake(0, self.view.frame.size.height-150, self.view.frame.size.width, 150);
                [driverInfoView smallViewFrame];
                isDown = YES;
            }];
        }
    }
    else
    {
        [UIView animateWithDuration:0.3 animations:^{
            driverInfoView.frame = CGRectMake(0, self.view.frame.size.height-200, self.view.frame.size.width, 200);
            [driverInfoView allViewFrame];
            isDown = NO;
        }];
    }
}

-(void)back:(id)sender
{
    //确认取消订单
    
    UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"提示" message:@"是否取消订单" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
    [alert show];
}
-(void)callCar
{
    //启动进度条
    NSDictionary *param = @ {@"cmd": @"create_order", @"role": @"2", @"start":_startLocation.name, @"destination":_endLocation.name,
                            @"start_lat":@(_startLocation.coordinate2D.latitude), @"start_lng":@(_startLocation.coordinate2D.longitude),
                            @"destination_lat":@(_endLocation.coordinate2D.latitude), @"destination_lng":@(_endLocation.coordinate2D.longitude),
                            @"pre_mileage":@(12), @"pre_price":@(65), @"car_type":@(1)};
    
    [[DDSocket currentSocket] sendRequest:param];
}

- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
{
    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
    _mapView.centerCoordinate = coord;
}
#pragma mark == Location Service Delegate

- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    [_mapView updateLocationData:userLocation];
}

/**
 *用户位置更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    //    NSLog(@"didUpdateUserLocation lat %f,long %f",userLocation.location.coordinate.latitude,userLocation.location.coordinate.longitude);
    [_mapView updateLocationData:userLocation];
    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
    [_mapView setCenterCoordinate:coord animated:YES];
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark
#pragma mark == Socket Response handeling
- (void)receiveResponseHandles:(NSNotification *)notification
{
    NSDictionary *responseDict = notification.userInfo;
    
    if(!responseDict)
        return;
    NSString *command = [responseDict objectForKey:@"cmd"];
    NSNumber *status = [responseDict objectForKey:@"status"];
    
    if([command isEqualToString:@"create_order_resp"])
    {
        if([status integerValue] == 1)
        {
            //没有错
        }
    }
    else if ([command isEqualToString:@"order_accept"])
    {
        //有接单
        if([status intValue] == 1)
        {
            NSDictionary * driver = responseDict[@"driver"];
            
            self.orderDriver = [[Driver alloc]initWithDic:driver];
            driverInfoView.driver = self.orderDriver;
            
            //出现不完全的司机VIew
            [UIView animateWithDuration:.3 animations:^{
                driverInfoView.frame = CGRectMake(0, self.view.frame.size.height-150, self.view.frame.size.width, 150);
                isDown = YES;
                [driverInfoView smallViewFrame];
            }];
        }
    }
    else if([command isEqualToString:@"cancel_order_resp"])
    {
        if([status integerValue] == 1 || [status intValue] == -101)
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
        else if([status intValue] == -102)
        {
            NSLog(@"不能取消");
        }
    }
    else if([command isEqualToString:@"current_charge"])
    {
//        PaymentViewController *payVC = [[PaymentViewController alloc]initWithNibName:@"PaymentViewController" bundle:nil];
//        payVC.mileage = [responseDict objectForKey:@"current_mile"];
//        payVC.chargePrice = [responseDict objectForKey:@"current_charge"];
//        payVC.lowSpeedTime = [responseDict objectForKey:@"low_speed_time"];
//        [self.navigationController pushViewController:payVC animated:YES];
    }
    else if([command isEqualToString:@"order_end"])
    {
        
        //        (lldb) po responseDict
        //        {
        //            cmd = "order_end";
        //            order =     {
        //                "add_price1" = "<null>";
        //                "add_price2" = "<null>";
        //                "add_price3" = "<null>";
        //                "additional_price" = "0.00";
        //                "car_type" = 1;
        //                "cityline_id" = 0;
        //                "create_time" = 1449920275;
        //                destination = "\U5317\U4eac\U5c55\U89c8\U9986";
        //                "destination_lat" = "40.06377";
        //                "destination_lng" = "116.32138";
        //                "driver_id" = 3;
        //                "end_time" = 1449920506;
        //                id = 1154;
        //                isCancel = 0;
        //                isCityline = 0;
        //                "low_speed_time" = "<null>";
        //                mileage = "4.650858443200497";
        //                orderNum = 2015121219414697995097;
        //                "org_price" = "0.02";
        //                "passenger_id" = 2;
        //                "passenger_mobile" = 13700000002;
        //                "pay_role" = 2;
        //                "pay_time" = 0;
        //                "pay_type" = 0;
        //                "pre_mileage" = "12.00";
        //                "pre_price" = "65.00";
        //                rating = 0;
        //                "rent_type" = 0;
        //                start = "\U897f\U4e8c\U65d7\U5317\U8def";
        //                "start_lat" = "40.063761";
        //                "start_lng" = "116.321411";
        //                "start_time" = 1449920378;
        //                status = 4;
        //                sumprice = "0.02";
        //            };
        
        PaymentViewController *payVC = [[PaymentViewController alloc]initWithNibName:@"PaymentViewController" bundle:nil];
        payVC.mileage = [responseDict objectForKey:@"current_mile"];
        payVC.chargePrice = [responseDict objectForKey:@"current_charge"];
        payVC.lowSpeedTime = [responseDict objectForKey:@"low_speed_time"];
        [self.navigationController pushViewController:payVC animated:YES];
    }
    else if([command isEqualToString:@"driver_pay"])//{"cmd":"driver_pay","order_id":1236,"status":1}
    {
        
    }
    
    
}


@end
