//
//  DDMainViewController.m
//  duducar
//
//  Created by wenpeifang on 15/12/8.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "DDMainViewController.h"
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>
#import "DDSearchTableViewController.h"
#import "LoginViewController.h"
#import "PostOrderViewController.h"
#import "DDSocket.h"
#import "DDDatabase.h"
#import "UIColor+RCColor.h"
#import "Masonry.h"
#import "DDLog.h"
#import "DDTTYLogger.h"

@interface DDMainViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate>
{
    BMKGeoCodeSearch* _geocodesearch;
    UIButton *startPointSearchButton;
    UIButton *endPointSearchButton;
    UIButton *callCabButton;
    
    BMKPoiInfo *startLocation;
    BMKPoiInfo *endLocation;
    
    NSString *currCity;
}
@property (nonatomic,strong)BMKMapView* mapView ;
@property (nonatomic,strong)BMKLocationService *locService;
@end

static NSString * responseNotificationName = @"DDSocketResponseNotification";
// Log levels: off, error, warn, info, verbose
static const int ddLogLevel = LOG_LEVEL_VERBOSE;

@implementation DDMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    _geocodesearch = [[BMKGeoCodeSearch alloc]init];
    _geocodesearch.delegate =self;
    
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    [_locService startUserLocationService];
    
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width
            , self.view.frame.size.height)];
    _mapView.zoomLevel = 15;
    
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    

    startPointSearchButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [startPointSearchButton addTarget:self action:@selector(searchStartLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    startPointSearchButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    startPointSearchButton.imageView.contentMode = UIViewContentModeCenter;
    [startPointSearchButton setTitle:@"搜索起点" forState:UIControlStateNormal];
    [startPointSearchButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    [self.view addSubview:startPointSearchButton];
    
    endPointSearchButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [endPointSearchButton addTarget:self action:@selector(searchEndLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    //    [loginButton setBackgroundImage:[UIImage imageNamed:@"login_button"] forState:UIControlStateNormal];
    endPointSearchButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    endPointSearchButton.imageView.contentMode = UIViewContentModeCenter;
    [endPointSearchButton setTitle:@"搜索终点" forState:UIControlStateNormal];
    [endPointSearchButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    [self.view addSubview:endPointSearchButton];

    callCabButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [callCabButton addTarget:self action:@selector(callForCab:) forControlEvents:UIControlEventTouchUpInside];
    //    [loginButton setBackgroundImage:[UIImage imageNamed:@"login_button"] forState:UIControlStateNormal];
    callCabButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    callCabButton.imageView.contentMode = UIViewContentModeCenter;
    [callCabButton setTitle:@"确认叫车" forState:UIControlStateNormal];
    [callCabButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    [self.view addSubview:callCabButton];
    
    double buttonWidth = [UIScreen mainScreen].bounds.size.width/2;
    
    [callCabButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.width.mas_equalTo(buttonWidth);
        make.height.mas_equalTo(40);
        make.bottom.mas_equalTo(self.view.mas_bottom).offset(-50);
    }];
    
    [endPointSearchButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.width.mas_equalTo(buttonWidth);
        make.height.mas_equalTo(40);
        make.bottom.mas_equalTo(callCabButton.mas_top).offset(-8);
    }];
    
    [startPointSearchButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.width.mas_equalTo(buttonWidth);
        make.height.mas_equalTo(40);
        make.bottom.mas_equalTo(endPointSearchButton.mas_top).offset(-8);
    }];
    
    // 叫车大头针
    UIView * view1 = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 10, 10)];
    view1.backgroundColor = [UIColor redColor];
    view1.center = self.view.center;
    [self.view addSubview:view1];
    
    [[DDSocket currentSocket] startSocket];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
    
    [DDLog addLogger:[DDTTYLogger sharedInstance]];
}

-(void)viewWillAppear:(BOOL)animated {
    [_mapView viewWillAppear];
    _mapView.delegate = self; // 此处记得不用的时候需要置nil，否则影响内存的释放
}

-(void)viewWillDisappear:(BOOL)animated {
    [_mapView viewWillDisappear];
    _mapView.delegate = nil; // 不用时，置nil
}

- (void)dealloc {
    if (_mapView) {
        _mapView = nil;
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark
#pragma mark == Geocoder Delegate

- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error
{
    currCity = result.addressDetail.city;
    NSLog(@"当前城市:%@",currCity);
}


#pragma mark
#pragma mark == MapView Delegate
- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    CGPoint centerPosition = self.view.center;
    CLLocationCoordinate2D  coord = [_mapView convertPoint:centerPosition toCoordinateFromView:self.view];
    
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = coord;
    BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
    if(flag)
    {
        NSLog(@"反geo检索发送成功");
    }
    else
    {
        NSLog(@"反geo检索发送失败");
    }
    
    // 获得当前地理位置
    // 获得最近的车辆
    
}
- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
{
    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
    _mapView.centerCoordinate = coord;

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
#pragma mark == User Actions
-(void)searchStartLocationButtonClicked:(id)sender
{
    DDSearchTableViewController *searchVC = [[DDSearchTableViewController alloc]initWithNibName:@"DDSearchTableViewController" bundle:nil];
    searchVC.currCity = currCity;
    [searchVC setStartPointCompletionHandler:^(BMKPoiInfo *startPoint) {
        startLocation = startPoint;
        startPointSearchButton.titleLabel.text = startLocation.name;
    }];
    
    [searchVC setEndPointCompletionHandler:nil];
    
    [[self navigationController] pushViewController:searchVC animated:YES];
}

-(void)searchEndLocationButtonClicked:(id)sender
{
    DDSearchTableViewController *searchVC = [[DDSearchTableViewController alloc]initWithNibName:@"DDSearchTableViewController" bundle:nil];
    
    searchVC.currCity = currCity;
    
    [searchVC setEndPointCompletionHandler:^(BMKPoiInfo *endPoint) {
        endLocation = endPoint;
        endPointSearchButton.titleLabel.text = endLocation.name;
    }];
    
    [searchVC setStartPointCompletionHandler:nil];
    
    [[self navigationController] pushViewController:searchVC animated:YES];
}

-(void)callForCab:(id)sender
{
    
    [[DDDatabase sharedDatabase] selectFromPersonInfo:^(NSString *token, NSString *phone) {
        if(token==nil || phone == nil)
        {
            LoginViewController *loginVC = [[LoginViewController alloc]init];
            [self.navigationController pushViewController:loginVC animated:YES];
        } else {
            NSDictionary *paramDict = @{@"cmd":@"login", @"role":@"2", @"mobile":phone, @"token":token};
            [[DDSocket currentSocket] sendLoginRequest:paramDict];
        }
    }];
}

#pragma mark 
#pragma mark == Location Service Delegate
/**
 *在地图View将要启动定位时，会调用此函数
 *@param mapView 地图View
 */
- (void)willStartLocatingUser
{
    NSLog(@"start locate");
}

/**
 *用户方向更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    [_mapView updateLocationData:userLocation];
//    NSLog(@"heading is %@",userLocation.heading);
}

/**
 *用户位置更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    //    NSLog(@"didUpdateUserLocation lat %f,long %f",userLocation.location.coordinate.latitude,userLocation.location.coordinate.longitude);
    [_mapView updateLocationData:userLocation];
}

/**
 *在地图View停止定位后，会调用此函数
 *@param mapView 地图View
 */
- (void)didStopLocatingUser
{
    NSLog(@"stop locate");
}

/**
 *定位失败后，会调用此函数
 *@param mapView 地图View
 *@param error 错误号，参考CLError.h中定义的错误号
 */
- (void)didFailToLocateUserWithError:(NSError *)error
{
    NSLog(@"location error");
}

- (void)receiveResponseHandles:(NSNotification *)notification
{
    NSDictionary *responseDict = notification.userInfo;
    NSString *command = [responseDict objectForKey:@"cmd"];
    NSNumber *status = [responseDict objectForKey:@"status"];
    
    if([command isEqualToString:@"login_resp"])
    {
        if([status intValue] == 1)
        {
            NSString *activeOrderJson;
            
            if([responseDict objectForKey:@"active_order"])
            {
                activeOrderJson = [responseDict objectForKey:@"active_order"];
            
                //Deserialastion a Json String into Dictionary
                NSError *jsonError;
                NSData  *objectData = [activeOrderJson dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *activeOrder = [NSJSONSerialization JSONObjectWithData:objectData
                                                                             options:NSJSONReadingMutableContainers
                                                                               error:&jsonError];
                PostOrderViewController *postVC = [[PostOrderViewController alloc] init];
                postVC.activeOrder = activeOrder;
                [self.navigationController pushViewController:postVC animated:YES];
            } else {
            
            //Login Succedded, 直接叫车
            NSDictionary *param = @{@"cmd": @"create_order", @"role": @"2", @"start":startLocation.name, @"destination":endLocation.name,
                                    @"start_lat":@(startLocation.pt.latitude), @"start_lng":@(startLocation.pt.longitude),
                                    @"destination_lat":@(endLocation.pt.latitude), @"destination_lng":@(endLocation.pt.longitude),
                                    @"pre_mileage":@(12), @"pre_price":@(65), @"car_type":@(1)};
            
            [[DDSocket currentSocket] sendCarRequest:param];
            }
            
        } else {
            //弹出登陆界面
            LoginViewController *loginVC = [[LoginViewController alloc]init];
            [self.navigationController pushViewController:loginVC animated:YES];
        }
    }
    
}


@end
