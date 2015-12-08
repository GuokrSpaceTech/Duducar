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
@interface DDMainViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate>
@property (nonatomic,strong)BMKMapView* mapView ;
@property (nonatomic,strong)BMKLocationService *locService;
@end

@implementation DDMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width
            , self.view.frame.size.height)];
    _mapView.showsUserLocation = YES;
    
    [self.view addSubview:_mapView];
    
    
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    //启动LocationService
    [_locService startUserLocationService];
                
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
-(void)viewWillAppear:(BOOL)animated
{
    [_mapView viewWillAppear];
    _mapView.delegate = self; // 此处记得不用的时候需要置nil，否则影响内存的释放
}
-(void)viewWillDisappear:(BOOL)animated
{
    [_mapView viewWillDisappear];
    _mapView.delegate = nil; // 不用时，置nil
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
