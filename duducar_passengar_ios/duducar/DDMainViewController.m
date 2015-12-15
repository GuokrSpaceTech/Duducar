//
//  DDMainViewController.m
//  主要功能：
//  - 自动定位
//  - 搜索起点 和 终点
//  - 进入叫车页面
//  - 进入侧拉辅助页面
//  - 定期获取周边专车
//  Created by wenpeifang on 15/12/8.
//  Copyright © 2015年 guokrspace. All rights reserved.
//
#import "DDMainViewController.h"
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import "DDSearchTableViewController.h"
#import "LoginViewController.h"
#import "PostOrderViewController.h"
#import "CostEstimationViewController.h"
#import "DDSocket.h"
#import "DDDatabase.h"
#import "UIColor+RCColor.h"
#import "Masonry.h"
#import "DDLog.h"
#import "DDTTYLogger.h"
#import "DDLeftView.h"
#import "Masonry.h"

#import "PersionInfoViewController.h"
#import "HisoryViewController.h"
#import "Location.h"

#import "CallCarViewController.h"
#import "OrderInfoView.h"
@interface DDMainViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate,LeftViewDelegate>
{
    BMKGeoCodeSearch* _geocodesearch;
    BMKMapView* _mapView ;
    BMKLocationService *_locService;
    
    NSString *currCity;
    Location * startLocation;
    Location * endLocation;
    Location * currMapCenterLocation;
    BMKUserLocation *currentLoc;
    
    BOOL isLoginSuccess;
    
    UIButton *startLocButton;
    UIButton *stopLocButton;
    UIButton *callCabButton;
    UIButton *estCostButton;
    
    DDLeftView * leftView;
    BOOL leftViewShow;

    NSTimer *_repeatingTimer;
    NSUInteger _timerCount;
}

-(void)countedTimerAction:(NSTimer*)theTimer;
-(void)searchStartLocationButtonClicked:(id)sender;
-(void)searchEndLocationButtonClicked:(id)sender;
-(void)callForCab:(id)sender;
@end

static NSString * responseNotificationName = @"DDSocketResponseNotification";

@implementation DDMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    /*
     * Init UI
     */
    self.view.backgroundColor = [UIColor whiteColor];
    
    // 左侧按钮
    UIBarButtonItem * leftItem  = [[UIBarButtonItem alloc]initWithTitle:@"left" style:UIBarButtonItemStyleDone target:self action:@selector(leftCilck:)];
    self.navigationItem.leftBarButtonItem = leftItem;
    leftView = [[DDLeftView alloc]initWithFrame:CGRectMake(-self.view.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height)];
    leftView.backgroundColor = [UIColor clearColor];
    leftView.delegate = self;
    [self.view addSubview:leftView];
    leftViewShow = NO;
    
    //Map View
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _mapView.zoomLevel = 15;
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeFollow;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    
    // 定位按钮
    UIButton *locButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [locButton setTitle:@"定位" forState:UIControlStateNormal];
    [locButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [locButton addTarget:self action:@selector(locButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:locButton];

    
    //起点 终点
    UIView * backView = [[UIView alloc]initWithFrame:CGRectMake(10, self.view.frame.size.height - 170, self.view.frame.size.width-20, 100)];
    backView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:backView];
    
    startLocButton = [UIButton buttonWithType:UIButtonTypeCustom];
    startLocButton.frame = CGRectMake(0, 0, backView.frame.size.width, backView.frame.size.height/2.0);
    [startLocButton setTitle:@"输入起点" forState:UIControlStateNormal];
    [startLocButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [startLocButton addTarget:self action:@selector(searchStartLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    [backView addSubview:startLocButton];
    
    stopLocButton = [UIButton buttonWithType:UIButtonTypeCustom];
    stopLocButton.frame = CGRectMake(0, backView.frame.size.height/2.0, backView.frame.size.width, backView.frame.size.height/2.0);
    [stopLocButton setTitle:@"输入终点" forState:UIControlStateNormal];
    [stopLocButton addTarget:self action:@selector(searchEndLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    [stopLocButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [backView addSubview:stopLocButton];
    
    // 费用估算按钮
    estCostButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [estCostButton setTitle:@"费用估算" forState:UIControlStateNormal];
    [estCostButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [estCostButton addTarget:self action:@selector(estimateButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    estCostButton.enabled = false;
    [self.view addSubview:estCostButton];
    
    //叫车按钮
    callCabButton = [UIButton buttonWithType:UIButtonTypeSystem];
    callCabButton.frame = CGRectMake(10, self.view.frame.size.height - 60, self.view.frame.size.width-20, 40);
    callCabButton.backgroundColor = [UIColor blueColor];
    [callCabButton setTitle:@"呼叫专车" forState:UIControlStateNormal];
    [callCabButton addTarget:self action:@selector(callForCab:) forControlEvents:UIControlEventTouchUpInside];
    [callCabButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    callCabButton.userInteractionEnabled = NO;
    [self.view addSubview:callCabButton];
    
    // 当前未知大头针
    UIView * view1 = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 10, 10)];
    view1.backgroundColor = [UIColor redColor];
    view1.center = self.view.center;
    [self.view addSubview:view1];
    
    //Add Constrains
    [locButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(@60);
        make.width.mas_equalTo(@60);
        make.bottom.equalTo(backView.mas_top).offset(-8);
        make.right.equalTo(backView.mas_right);
    }];
    
    [estCostButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(@30);
        make.width.mas_equalTo(@60);
        make.centerY.equalTo(stopLocButton.mas_centerY);
        make.right.equalTo(stopLocButton.mas_right);
    }];

    /*
     * nit Data
     */
    startLocation = [[Location alloc]init];
    endLocation = [[Location alloc]init];
    currMapCenterLocation = [[Location alloc]init];

    /*
     * Geocoder Init
     */
    _geocodesearch = [[BMKGeoCodeSearch alloc]init];
    _geocodesearch.delegate =self;
    
    /*
     * Location Service init
     */
     _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    [_locService startUserLocationService];
    

}


-(void)viewWillAppear:(BOOL)animated {
    [_mapView viewWillAppear];
    _mapView.delegate = self; // 此处记得不用的时候需要置nil，否则影响内存的释放
    /*
     * 监听来自Socket的服务消息
     */
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
}

-(void)viewWillDisappear:(BOOL)animated {
    [_mapView viewWillDisappear];
    _mapView.delegate = nil; // 不用时，置nil
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)dealloc {
    if (_mapView) {
        _mapView = nil;
    }
    
    if(_repeatingTimer!=nil)
        _repeatingTimer = nil;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark
#pragma mark == Geocoder Delegate
- (void)onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error
{
    if(error == BMK_SEARCH_NO_ERROR)
    {
        BMKAddressComponent * component = result.addressDetail;
        NSString * address = [NSString stringWithFormat:@"%@%@",component.streetName,component.streetNumber];
        if([address isEqualToString:@""])
        {
            address = [NSString stringWithFormat:@"%@",component.district];
        }
        [startLocButton setTitle:address forState:UIControlStateNormal];
        
        startLocation.name = address;
        startLocation.coordinate2D = result.location;
        
        currMapCenterLocation.name = address;
        currMapCenterLocation.coordinate2D = result.location;
        
        currCity = result.addressDetail.city;
        NSLog(@"当前城市:%@, 当前地址:%@",currCity, address);
    }
}


#pragma mark
#pragma mark == MapView Delegate
- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    CGPoint centerPosition = self.view.center;
    CLLocationCoordinate2D  coord = [_mapView convertPoint:centerPosition toCoordinateFromView:self.view];
    
    startLocation.coordinate2D = coord;
    
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = coord;
    [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
}

- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
{
}

// 根据anntation生成对应的View
- (BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id <BMKAnnotation>)annotation
{
    //普通annotation
    
    NSString *AnnotationViewID = @"renameMark";
    BMKPinAnnotationView *annotationView = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:AnnotationViewID];
    if (annotationView == nil) {
        annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:AnnotationViewID];
        // 设置颜色
        annotationView.pinColor = BMKPinAnnotationColorPurple;
        // 从天上掉下效果
        annotationView.animatesDrop = NO;
        // 设置可拖拽
        annotationView.draggable = NO;
    }
    return annotationView;

    //动画annotation
}

#pragma mark
#pragma mark == Location Service Delegate
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
    NSLog(@"didUpdateUserLocation lat %f,long %f",userLocation.location.coordinate.latitude,userLocation.location.coordinate.longitude);
    [_mapView updateLocationData:userLocation];
    currentLoc = userLocation;
    
}

#pragma mark
#pragma mark == User Actions
-(void)searchStartLocationButtonClicked:(id)sender
{
    DDSearchTableViewController *searchVC = [[DDSearchTableViewController alloc]initWithNibName:@"DDSearchTableViewController" bundle:nil];
    searchVC.currCity = currCity;
    [searchVC setStartPointCompletionHandler:^(BMKPoiInfo *startPoint) {
        //startLocation = startPoint;
        startLocation.coordinate2D = startPoint.pt;
        startLocation.name = startPoint.name;
        startLocButton.titleLabel.text = startLocation.name;
        [_mapView setCenterCoordinate:startPoint.pt animated:YES];
    }];
    
    [searchVC setEndPointCompletionHandler:nil];
    
    [[self navigationController] pushViewController:searchVC animated:YES];
}

-(void)searchEndLocationButtonClicked:(id)sender
{
    DDSearchTableViewController *searchVC = [[DDSearchTableViewController alloc]initWithNibName:@"DDSearchTableViewController" bundle:nil];
    
    searchVC.currCity = currCity;
    
    [searchVC setEndPointCompletionHandler:^(BMKPoiInfo *endPoint) {
        endLocation.coordinate2D = endPoint.pt;
        endLocation.name = endPoint.name;
        stopLocButton.titleLabel.text = endLocation.name;
        callCabButton.userInteractionEnabled = YES;
        estCostButton.enabled = true;
    }];
    
    [searchVC setStartPointCompletionHandler:nil];
    
    [[self navigationController] pushViewController:searchVC animated:YES];
}

-(void)callForCab:(id)sender
{
    if(isLoginSuccess)
    {
        CallCarViewController * carVC = [[CallCarViewController alloc]init] ;
        carVC.startLocation = startLocation;
        carVC.endLocation = endLocation;
        [self.navigationController pushViewController:carVC animated:YES];
    } else {
        LoginViewController *loginVC = [[LoginViewController alloc]init];
        [self.navigationController pushViewController:loginVC animated:YES];
    }
}

-(void)locButtonClick:(id)sender
{
    [_mapView updateLocationData:currentLoc];
    
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeFollow;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = currentLoc.location.coordinate;;
    [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
    
}

-(void)estimateButtonClick:(id)sender
{
    CostEstimationViewController *costVC = [[CostEstimationViewController alloc]initWithNibName:@"CostEstimationViewController" bundle:nil];
    costVC.startLoc = startLocation;
    costVC.endLoc   = endLocation;
    costVC.city = currCity;
    [self.navigationController pushViewController:costVC animated:YES];
}

-(void)leftCilck:(id)sender
{
    if(leftViewShow == NO)
    {
        leftViewShow = YES;
        [UIView animateWithDuration:0.3 animations:^{
            leftView.frame = CGRectMake(0, 0,self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
    else
    {
        leftViewShow = NO;
        [UIView animateWithDuration:0.3 animations:^{
            leftView.frame = CGRectMake(-self.view.frame.size.width, 0,self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
}

#pragma mark
#pragma mark == Receive Notification from Socket Response data
- (void)receiveResponseHandles:(NSNotification *)notification
{
    NSDictionary *responseDict = notification.userInfo;
    
    if(!responseDict)
        return;
    
    NSString *command = [responseDict objectForKey:@"cmd"];
    NSNumber *status = [responseDict objectForKey:@"status"];
    
    //Initial Message From Server {status: 1}。连接成功，如果已经保存Token，直接发起Login请求。
    if(command == nil)
    {
        if([status intValue] == 1)
        {
            [[DDDatabase sharedDatabase] selectFromPersonInfo:^(NSString *token, NSString *phone) {
                if(token==nil || phone == nil)
                {
                    isLoginSuccess = false;
                } else {
                    NSDictionary *paramDict = @{@"cmd":@"login", @"role":@"2", @"mobile":phone, @"token":token};
                    [[DDSocket currentSocket] sendRequest:paramDict];
                }
            }];
            
            //Kick off the timer。 定期获得周边车辆信息。
            _timerCount = 5;
            _repeatingTimer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(countedTimerAction:) userInfo:nil repeats:YES];
        }
    }
    else if([command isEqualToString:@"login_resp"])
    {
        if([status intValue] == 1)
        {
            isLoginSuccess = true;
            //如果有当前活跃订单，直接进入叫车界面。
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
                CallCarViewController *postVC = [[CallCarViewController alloc] init];
                postVC.activeOrder = activeOrder;
                [self.navigationController pushViewController:postVC animated:YES];
            }
        } else {
            //自动登录出现错误
            isLoginSuccess = false;
        }
    }
    else if([command isEqualToString:@"get_near_car_resp"])
    {
        if([status intValue] == 1)
        {
            //获取附近车辆信息
            [_mapView removeAnnotations:_mapView.annotations];
            NSArray * arr = responseDict[@"cars"];
            if([arr isKindOfClass:[NSArray class]])
            {
                for (int i = 0; i < arr.count ; i++) {
                    NSDictionary * dic = arr[i];
                    double lat = [dic[@"lat"] doubleValue];
                    double lng = [dic[@"lng"] doubleValue];
                    
                    //添加标注
                    BMKPointAnnotation *pointAnnotation = [[BMKPointAnnotation alloc]init];
                    CLLocationCoordinate2D coor;
                    coor.latitude = lat;
                    coor.longitude = lng;
                    pointAnnotation.coordinate = coor;
                    [_mapView addAnnotation:pointAnnotation];
                }
            }
        }
    }
}

#pragma mark 
#pragma mark ==== leftView delegate ===
-(void)leftViewClose:(DDLeftView *)leftView
{
    [self leftViewDisappear];
}
-(void)leftView:(DDLeftView *)leftView index:(NSInteger)index
{
     [self leftViewDisappear];
    if(index == 0)
    {
        PersionInfoViewController * persionVC = [[PersionInfoViewController alloc]init];
        [self.navigationController pushViewController:persionVC animated:YES];
    }
    else
    {
        HisoryViewController * persionVC = [[HisoryViewController alloc]init];
        [self.navigationController pushViewController:persionVC animated:YES];
    }
}
-(void)leftViewDisappear
{
    leftViewShow = NO;
    [UIView animateWithDuration:0.3 animations:^{
        leftView.frame = CGRectMake(-self.view.frame.size.width, 0,self.view.frame.size.width, self.view.frame.size.height);
    }];
    
}

#pragma mark - TIMER Handles

-(void)countedTimerAction:(NSTimer *)timer
{
    _timerCount --;
    
    if(_timerCount==0 && currMapCenterLocation!=nil)
    {
        _timerCount = 5;
        
        NSDictionary *postDictionary = [NSDictionary dictionaryWithObjects:@[@"get_near_car",@(currMapCenterLocation.coordinate2D.latitude),@(currMapCenterLocation.coordinate2D.longitude),@"1", @"2"] forKeys:@[@"cmd",@"lat",@"lng",@"car_type", @"role"]];
        
        NSError * error = nil;
        NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
        
        NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        [jsonString appendString:@"\n"];
        NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
        
//        [[DDSocket currentSocket]sendData:outStr timeOut:-1.0 tag:0];
    }
}
@end
