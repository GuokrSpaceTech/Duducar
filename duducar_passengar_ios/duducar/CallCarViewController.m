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
#import "RatingViewController.h"
@interface CallCarViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,UIAlertViewDelegate>
{
    BMKLocationService *_locService;
    BMKMapView * _mapView;
    
    StartEndView * startEndView;
    
    OrderInfoView * driverInfoView;

    BOOL isDown; //是否是向下滑
    
    BMKUserLocation *currentUserLocation;
    
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
    //导航条
    UIBarButtonItem * leftItem = [[UIBarButtonItem alloc]initWithTitle:@"取消叫车" style:UIBarButtonItemStyleDone target:self action:@selector(back:)];
    self.navigationItem.leftBarButtonItem = leftItem;
    self.navigationItem.title = @"正在叫车......";
    
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _mapView.zoomLevel = 14;
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
        if([orderStatus isEqualToString:@"2"])
        {
            self.navigationItem.title = @"司机已经接单";
        }
        else if([orderStatus isEqualToString:@"3"])
        {
            self.navigationItem.title = @"乘客已经上车";
        }
        else if([orderStatus isEqualToString:@"3"])
        {
            self.navigationItem.title = @"已经到达目的地";
        }
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
    NSDictionary *param = @ {@"cmd": @"create_order", @"role": @"2", @"start":_startLocation.name, @"destination":_endLocation.name,
                            @"start_lat":@(_startLocation.coordinate2D.latitude), @"start_lng":@(_startLocation.coordinate2D.longitude),
                            @"destination_lat":@(_endLocation.coordinate2D.latitude), @"destination_lng":@(_endLocation.coordinate2D.longitude),
                            @"pre_mileage":@(0), @"pre_price":@(65), @"car_type":@(1)};
    
    [[DDSocket currentSocket] sendRequest:param];
}
#pragma mark
#pragma mark == MapView Delegate
-(BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id<BMKAnnotation>)annotation
{
    if ([annotation isKindOfClass:[BMKPointAnnotation class]]) {
        
        BMKPinAnnotationView *newAnnotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"car"];
        
        newAnnotationView.pinColor = BMKPinAnnotationColorPurple;
        
        newAnnotationView.animatesDrop = NO;// 设置该标注点动画显示
        
        newAnnotationView.annotation=annotation;
        
        newAnnotationView.image = [UIImage imageNamed:@"car_icon"];   //把大头针换成别的图片
        
        return newAnnotationView;
    }
    
    return nil;
}
- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
{
    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
    _mapView.centerCoordinate = coord;
}

#pragma mark
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
    
    //保存全局变量
    currentUserLocation = userLocation;
}

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

            self.navigationItem.title = @"司机已经接单";
        }
    }
    else if([command isEqualToString:@"order_start"])
    {
        self.navigationItem.title = @"乘客已经上车";
    }
    else if([command isEqualToString:@"cancel_order_resp"])
    {
        if([status integerValue] == 1 || [status intValue] == -101)
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
        else if([status intValue] == -102)
        {
            self.navigationItem.leftBarButtonItem.enabled = false;
            NSLog(@"不能取消");
        }
    }
    else if([command isEqualToString:@"current_charge"])
    {
        NSString *currentCharge;
        NSString *currentMileage;
        if([[responseDict objectForKey:@"current_charge"] isKindOfClass:[NSString class]])
        {
            currentCharge = [responseDict objectForKey:@"current_charge"];
        }
        if([[responseDict objectForKey:@"current_mile"] isKindOfClass:[NSString class]])
        {
            currentMileage = [responseDict objectForKey:@"current_mile"];
        }
        //添加标注
        if(currentUserLocation)
        {
            BMKPointAnnotation *pointAnnotation = [[BMKPointAnnotation alloc]init];
            pointAnnotation.coordinate = currentUserLocation.location.coordinate;
            pointAnnotation.title = [NSString stringWithFormat:@"当前里程%@, 当前金额:%@", currentMileage, currentCharge];
            [_mapView addAnnotation:pointAnnotation];
        }
    }
    else if([command isEqualToString:@"order_end"])
    {
        self.navigationItem.title = @"到达目的地";
        NSDictionary *order = [responseDict objectForKey:@"order"];
        NSNumber *payrole = [order objectForKey:@"pay_role"];
        
        //司机代支付
        if([payrole intValue] == 1)
        {
            //防止重复调用评价界面
            if(![[self.navigationController topViewController] isKindOfClass:[RatingViewController class]])
            {
                RatingViewController *rateVC = [[RatingViewController alloc]initWithNibName:@"RatingViewController" bundle:nil];
                rateVC.activeOrder = order;
                rateVC.driver = self.orderDriver;
                [self.navigationController pushViewController:rateVC animated:YES];
            }
        }
        else
        {
            //防止重复调用支付界面
            if(![[self.navigationController topViewController] isKindOfClass:[PaymentViewController class]])
            {
                PaymentViewController *payVC = [[PaymentViewController alloc]initWithNibName:@"PaymentViewController" bundle:nil];
                payVC.activeOrder = order;
                payVC.driver = self.orderDriver;
                [self.navigationController pushViewController:payVC animated:YES];
            }
        }
    }
}
@end
