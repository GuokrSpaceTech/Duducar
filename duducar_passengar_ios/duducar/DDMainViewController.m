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
#import "UIColor+RCColor.h"
#import "Masonry.h"
@interface DDMainViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate>
{
    BMKGeoCodeSearch* _geocodesearch;
    UIButton *startPointSearchButton;
    UIButton *endPointSearchButton;
    UIButton *callCabButton;
}
@property (nonatomic,strong)BMKMapView* mapView ;
@property (nonatomic,strong)BMKLocationService *locService;
@end

@implementation DDMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _geocodesearch = [[BMKGeoCodeSearch alloc]init];
    _geocodesearch.delegate =self;
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    [_locService startUserLocationService];
    
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width
            , self.view.frame.size.height)];
    _mapView.zoomLevel = 15;
    
    _mapView.userTrackingMode = BMKUserTrackingModeNone;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    

    startPointSearchButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [startPointSearchButton addTarget:self action:@selector(searchButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    startPointSearchButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    startPointSearchButton.imageView.contentMode = UIViewContentModeCenter;
    [startPointSearchButton setTitle:@"搜索起点" forState:UIControlStateNormal];
    [startPointSearchButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    [self.view addSubview:startPointSearchButton];
    
    endPointSearchButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [endPointSearchButton addTarget:self action:@selector(searchButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    //    [loginButton setBackgroundImage:[UIImage imageNamed:@"login_button"] forState:UIControlStateNormal];
    endPointSearchButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    endPointSearchButton.imageView.contentMode = UIViewContentModeCenter;
    [endPointSearchButton setTitle:@"搜索终点" forState:UIControlStateNormal];
    [endPointSearchButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    [self.view addSubview:endPointSearchButton];

    callCabButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [callCabButton addTarget:self action:@selector(searchButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
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
    
//    // 叫车大头针
//    UIView * view1 = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 10, 10)];
//    view1.backgroundColor = [UIColor redColor];
//    view1.center = self.view.center;
//    [self.view addSubview:view1];
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

//- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
//{
//    CGPoint centerPosition = self.view.center;
//    CLLocationCoordinate2D  coord = [_mapView convertPoint:centerPosition toCoordinateFromView:self.view];
//    
//    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
//    reverseGeocodeSearchOption.reverseGeoPoint = coord;
//    BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
//    if(flag)
//    {
//        NSLog(@"反geo检索发送成功");
//    }
//    else
//    {
//        NSLog(@"反geo检索发送失败");
//    }
//
//    // 获得当前地理位置
//    // 获得最近的车辆
//    
//}
- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error
{
    if(error == BMK_SEARCH_NO_ERROR)
    {
        
    }
}
//- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
//{
//    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
//    _mapView.centerCoordinate = coord;
//
//}
- (void)willStartLocatingUser
{

}
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    //NSLog(@"heading is %@",userLocation.heading);
    [_mapView updateLocationData:userLocation];
}
//处理位置坐标更新
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
      [_mapView updateLocationData:userLocation];
    //NSLog(@"didUpdateUserLocation lat %f,long %f",userLocation.location.coordinate.latitude,userLocation.location.coordinate.longitude);
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

-(void)searchButtonClicked:(id)sender
{
    DDSearchTableViewController *searchVC = [[DDSearchTableViewController alloc]initWithNibName:@"DDSearchTableViewController" bundle:nil];
    [[self navigationController] pushViewController:searchVC animated:YES];
}

@end
