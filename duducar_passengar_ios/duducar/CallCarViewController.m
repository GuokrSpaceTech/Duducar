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
@interface CallCarViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,UIAlertViewDelegate>
{
    BMKLocationService *_locService;
    BMKMapView * _mapView;
    
    StartEndView * startEndView;
    
    OrderInfoView * driverInfoView;

    BOOL isDown; //是否是向下滑
    
}
@end

@implementation CallCarViewController
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
             //更新UI
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
        //费用
//        {
////            cmd = "current_charge";
////            "current_charge" = "0.01";
////            "current_mile" = "0.0";
////            "low_speed_time" = 0;
//        }

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
    }
}

- (void)dealloc {
    if (_mapView) {
        _mapView = nil;
    }
}

#pragma mark -==== 取消订单=====
-(void)cancelOrder
{
    NSDictionary *param = @ {@"cmd": @"cancel_order", @"role": @"2"};
    
    [[DDSocket currentSocket] sendCarRequest:param];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex ==0 )
    {
        NSLog(@"取消");
    }else if(buttonIndex == 1)
    {
        //确定
        [self cancelOrder];
    }
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    UIBarButtonItem * leftItem = [[UIBarButtonItem alloc]initWithTitle:@"返回" style:UIBarButtonItemStyleDone target:self action:@selector(back:)];
    self.navigationItem.leftBarButtonItem = leftItem;
    
    _locService = [[BMKLocationService alloc]init];
    [_locService startUserLocationService];
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
    
    UISwipeGestureRecognizer * swap = [[UISwipeGestureRecognizer alloc]initWithTarget:self action:@selector(swap:)];
    swap.direction = UISwipeGestureRecognizerDirectionUp;
    swap.numberOfTouchesRequired =1;
    [driverInfoView addGestureRecognizer:swap];
    
    UISwipeGestureRecognizer * swap1 = [[UISwipeGestureRecognizer alloc]initWithTarget:self action:@selector(swap:)];
    swap1.direction = UISwipeGestureRecognizerDirectionDown;
    swap1.numberOfTouchesRequired =1;
    [driverInfoView addGestureRecognizer:swap1];
    
    [self callCar];
    
}

-(void)swap:(UISwipeGestureRecognizer *)tap
{
    if(tap.direction == UISwipeGestureRecognizerDirectionDown)
    {
        NSLog(@"sown");
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
        NSLog(@"up");
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
    
    [[DDSocket currentSocket] sendCarRequest:param];
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

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
