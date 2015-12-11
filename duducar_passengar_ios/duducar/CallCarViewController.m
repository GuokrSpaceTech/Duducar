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

@interface CallCarViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate>
{
    BMKLocationService *_locService;
    BMKMapView * _mapView;
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
    
    if([command isEqualToString:@"create_order"])
    {
        if([status integerValue] == 1)
        {
            //叫车成功
        }
    }
}

- (void)dealloc {
    if (_mapView) {
        _mapView = nil;
    }
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    _locService = [[BMKLocationService alloc]init];
    [_locService startUserLocationService];
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _mapView.zoomLevel = 15;
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    [self callCar];
    
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
