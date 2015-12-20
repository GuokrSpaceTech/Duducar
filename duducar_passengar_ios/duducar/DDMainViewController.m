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
#import "CostEstimationViewController.h"
#import "PaymentViewController.h"
#import "DDSocket.h"
#import "DDDatabase.h"
#import "UIColor+RCColor.h"
#import "Masonry.h"
#import "DDLog.h"
#import "DDTTYLogger.h"
#import "DDLeftView.h"
#import "Masonry.h"
#import "WebViewController.h"
#import "HistoryViewController.h"
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
    UILabel *nearCarsPromptLabel;
    UIImageView *nearCarPromtBackView;
    
    DDLeftView * leftView;
    BOOL leftViewShow;

    BOOL isTimerRunning;
    NSTimer *_repeatingTimer;
    NSUInteger _timerCount;
    
    NSDictionary *baseinfo;
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
    
    //导航条
    self.navigationItem.title = @"嘟嘟专车";

    //Map View
    _mapView = [[BMKMapView alloc]initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    _mapView.zoomLevel = 14;
    _mapView.showsUserLocation = NO;//先关闭显示的定位图层
    _mapView.userTrackingMode = BMKUserTrackingModeFollow;//设置定位的状态
    _mapView.showsUserLocation = YES;//显示定位图层
    [self.view addSubview:_mapView];
    
    // 定位按钮
    UIButton *locButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [locButton setImage:[UIImage imageNamed:@"ic_my_location"] forState:UIControlStateNormal];
    [locButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [locButton addTarget:self action:@selector(locButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:locButton];
    
    //起点 终点
    UIView * backView = [[UIView alloc]initWithFrame:CGRectMake(10, self.view.frame.size.height - 170, self.view.frame.size.width-20, 100)];
    backView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:backView];
    
    UIImageView *startIndicator = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"startIndicator"]];
    startIndicator.contentMode = UIViewContentModeScaleAspectFit;
    UIImageView *endIndicator = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"endIndicator"]];
    endIndicator.contentMode = UIViewContentModeScaleAspectFit;
    UIView *line = [[UIView alloc]init];
    line.backgroundColor = [UIColor lightGrayColor];
    
    [backView addSubview:line];
    [backView addSubview:startIndicator];
    [backView addSubview:endIndicator];
    
    startLocButton = [UIButton buttonWithType:UIButtonTypeCustom];
    startLocButton.frame = CGRectMake(0, 0, backView.frame.size.width, backView.frame.size.height/2.0);
    [startLocButton setTitle:@"正在定位" forState:UIControlStateNormal];
    [startLocButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [startLocButton addTarget:self action:@selector(searchStartLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    [backView addSubview:startLocButton];
    
    stopLocButton = [UIButton buttonWithType:UIButtonTypeCustom];
    stopLocButton.frame = CGRectMake(0, backView.frame.size.height/2.0, backView.frame.size.width, backView.frame.size.height/2.0);
    [stopLocButton setTitle:@"搜索终点" forState:UIControlStateNormal];
    [stopLocButton addTarget:self action:@selector(searchEndLocationButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    [stopLocButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [backView addSubview:stopLocButton];
    
    [startIndicator mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(backView.mas_left).offset(16);
        make.top.equalTo(startLocButton.mas_top).offset(8);
        make.centerY.equalTo(startLocButton);
        make.height.equalTo(startLocButton.mas_height);
        make.width.mas_equalTo(@8);
    }];
    
    [line mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(@1);
        make.centerY.equalTo(backView.mas_centerY);
        make.left.equalTo(backView.mas_left).offset(8);
        make.right.equalTo(backView.mas_right).offset(-8);
    }];
    
    [endIndicator mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(backView.mas_left).offset(16);
        make.top.equalTo(startIndicator.mas_bottom);
        make.bottom.equalTo(stopLocButton.mas_bottom).offset(-8);
        make.centerY.equalTo(stopLocButton);
        make.height.equalTo(stopLocButton.mas_height);
        make.width.mas_equalTo(@8);
    }];
    
    // 费用估算按钮
    estCostButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [estCostButton setImage:[UIImage imageNamed:@"ic_attach_money"] forState:UIControlStateNormal];
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
    callCabButton.enabled = false;
    [self.view addSubview:callCabButton];
    
    //中心指示
    UIImageView * centerView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, 24, 48)];
    centerView.image = [UIImage imageNamed:@"center"];
    [self.view addSubview:centerView];
    [centerView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.centerY.equalTo(self.view.mas_centerY).offset(-15);
    }];
    
    nearCarPromtBackView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"prompt"]];
    [self.view addSubview:nearCarPromtBackView];
    nearCarPromtBackView.contentMode = UIViewContentModeScaleAspectFit;
    nearCarsPromptLabel = [[UILabel alloc]init];
    nearCarsPromptLabel.textColor = [UIColor whiteColor];
    nearCarsPromptLabel.textAlignment = NSTextAlignmentCenter;
    nearCarsPromptLabel.font = [UIFont systemFontOfSize:14.0];
    [nearCarPromtBackView addSubview:nearCarsPromptLabel];
    
    [nearCarPromtBackView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self.view.mas_centerX);
        make.bottom.equalTo(centerView.mas_top).offset(-2);
    }];
    
    [nearCarsPromptLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(nearCarPromtBackView.mas_top).offset(2);
        make.bottom.equalTo(nearCarPromtBackView.mas_bottom).offset(-2);
        make.centerX.equalTo(nearCarPromtBackView.mas_centerX);
    }];
    
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
    
    // 左侧按钮
    UIBarButtonItem * leftItem  = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ic_view_headline_white"] style:UIBarButtonItemStyleDone target:self action:@selector(leftCilck:)];
    
    self.navigationItem.leftBarButtonItem = leftItem;
    leftView = [[DDLeftView alloc]initWithFrame:CGRectMake(-self.view.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height)];
    leftView.backgroundColor = [UIColor clearColor];
    
    leftView.delegate = self;
    [self.view addSubview:leftView];
    leftViewShow = NO;
    
    /*
     * Init Data
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
    isTimerRunning = true; // 开启Timer
    _mapView.delegate = self; // 此处记得不用的时候需要置nil，否则影响内存的释放
    /*
     * 监听来自Socket的服务消息
     */
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
}

-(void)viewWillDisappear:(BOOL)animated {
    [_mapView viewWillDisappear];
    _mapView.delegate = nil; // 不用时，置nil
    isTimerRunning = false; // 停止Timer
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
    } else {
        NSLog(@"ReverseGeoCodeResult 失败。");
        BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
        reverseGeocodeSearchOption.reverseGeoPoint = currentLoc.location.coordinate;
        [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
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
    CGPoint centerPosition = self.view.center;
    CLLocationCoordinate2D  coord = [_mapView convertPoint:centerPosition toCoordinateFromView:self.view];
    
    startLocation.coordinate2D = coord;
    
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = coord;
    [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
}

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
        callCabButton.enabled = true;
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
                    
                    [leftView setAvatarImage:@"" mobile:phone];
                }
            }];
            
            //Kick off the timer。 定期获得周边车辆信息。
            isTimerRunning = true;
            _timerCount = 5;
            _repeatingTimer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(countedTimerAction:) userInfo:nil repeats:YES];
        }
    }
    else if([command isEqualToString:@"login_resp"])
    {
        if([status intValue] == 1)
        {
            isLoginSuccess = true;
            NSString *activeOrderJson;
            NSString *driverJson;
            
            //Get Baseinfo
            NSDictionary *paramDict = @{@"cmd":@"baseinfo",@"role":@"2"};
            [[DDSocket currentSocket]sendRequest:paramDict];
            
            int activeOrderNum = [[responseDict objectForKey:@"has_active_order"] intValue];
            
            //如果有当前活跃订单
            if(activeOrderNum > 0)
            {
                activeOrderJson = [responseDict objectForKey:@"active_order"];
            
                //Deserialastion a Json String into Dictionary
                NSError *jsonError;
                NSData  *objectData = [activeOrderJson dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *activeOrder = [NSJSONSerialization JSONObjectWithData:objectData
                                                                        options:NSJSONReadingMutableContainers
                                                                          error:&jsonError];
                driverJson = [activeOrder objectForKey:@"driver"];
                
                //Deserialastion a Json String into Dictionary
                objectData = [driverJson dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *driverDict = [NSJSONSerialization JSONObjectWithData:objectData
                                                                           options:NSJSONReadingMutableContainers
                                                                             error:&jsonError];
                Driver *driver = [[Driver alloc]initWithDic:driverDict];
                
                //如果订单未结束
                /*
                const STATUS_INITATION 	    =1 ;
                const STATUS_ACCEPT 		=2 ;
                const STATUS_START 			=3 ;
                const STATUS_END 			=4 ;
                const STATUS_PAID			=5 ;
                const STATUS_CANCEL 		=-1 ;   //用户取消
                 */
                NSString *orderStatus = [activeOrder objectForKey:@"status"];
                //进行中订单
                if([orderStatus isEqualToString:@"1"] || [orderStatus isEqualToString:@"2"] || [orderStatus isEqualToString:@"3"])
                {
                    CallCarViewController *postVC = [[CallCarViewController alloc] init];
                    postVC.activeOrder = activeOrder;
                    [self.navigationController pushViewController:postVC animated:YES];
                }
                //已经支付
                else if([orderStatus isEqualToString:@"5"])
                {
                    //查看历史订单库
                }
                //未支付或者司机代付订单
                else if([orderStatus isEqualToString:@"4"])
                {
                    NSString *payrole = [activeOrder objectForKey:@"pay_role"];
                    //如果司机代付
                    if([payrole isEqualToString:@"1"])
                    {
                        //查看历史订单库
                    }
                    else
                    //需要支付
                    {
                        PaymentViewController *payVC = [[PaymentViewController alloc]initWithNibName:@"PaymentViewController" bundle:nil];
                        [payVC setActiveOrder:activeOrder];
                        [payVC setDriver:driver];
                        [self.navigationController pushViewController:payVC animated:YES];
                    }
                }
            }
        } else {
            //自动登录出现错误
            isLoginSuccess = false;
        }
    }
    //Socket直接存baseinfo到数据库
//    else if([command isEqualToString:@"baseinfo_resp"])
//    {
//        if([status intValue] == 1)
//        {
//            baseinfo = responseDict;
//        }
//    }
    else if([command isEqualToString:@"get_near_car_resp"])
    {
        if([status intValue] == 1)
        {
            //获取附近车辆信息
            [_mapView removeAnnotations:_mapView.annotations];
            NSArray * arr = responseDict[@"cars"];
            
            NSString *nearCarStr;
            if(arr.count == 0)
                nearCarStr= [NSString stringWithFormat:@"附近没有辆车"];
            else
                nearCarStr= [NSString stringWithFormat:@"附近有%d辆车",(int)arr.count];
            
            CGRect labelRect = [nearCarStr
                                boundingRectWithSize:CGSizeMake(200, 0)
                                options:NSStringDrawingUsesLineFragmentOrigin
                                attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:14.0]}
                                context:nil];
            
            [nearCarPromtBackView mas_updateConstraints:^(MASConstraintMaker *make) {
                make.width.mas_equalTo(labelRect.size.width + 12);
                make.height.mas_equalTo(labelRect.size.height + 4);
            }];
            
            nearCarsPromptLabel.text = nearCarStr;

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
    
    //读出baseinfo
    [[DDDatabase sharedDatabase]selectBaseinfo:^(NSString *baseinfostring) {
        
        //Deserialastion a Json String into Dictionary
        NSError *jsonError;
        NSData  *objectData = [baseinfostring dataUsingEncoding:NSUTF8StringEncoding];
        baseinfo = [NSJSONSerialization JSONObjectWithData:objectData
                                                   options:NSJSONReadingMutableContainers
                                                     error:&jsonError];
    }];
    
    if(index == 0)
    {
        HistoryViewController * histroyVC = [[HistoryViewController alloc]initWithNibName:@"HistoryViewController" bundle:nil];
        [self.navigationController pushViewController:histroyVC animated:YES];
    }
    else if(index == 1)
    {
        NSString *aboutusUrl = [[baseinfo objectForKey:@"webview"] objectForKey:@"about"];
        WebViewController *webVC = [[WebViewController alloc]init];
        webVC.urlStr = aboutusUrl;
        webVC.titleStr = @"关于我们";
        [self.navigationController pushViewController:webVC animated:YES];
    }
    else if(index == 2)
    {
        NSString *clauseUrl = [[baseinfo objectForKey:@"webview"] objectForKey:@"clause"];
        WebViewController *webVC = [[WebViewController alloc]init];
        webVC.titleStr = @"使用条款";
        webVC.urlStr = clauseUrl;
        [self.navigationController pushViewController:webVC animated:YES];
    }
    else if(index == 3)
    {
        NSString *helpUrl = [[baseinfo objectForKey:@"webview"] objectForKey:@"help"];
        WebViewController *webVC = [[WebViewController alloc]init];
        webVC.titleStr = @"帮助";
        webVC.urlStr = helpUrl;
        [self.navigationController pushViewController:webVC animated:YES];
    }
    else if(index == 4)
    {
        [[DDDatabase sharedDatabase]clearTable];
        LoginViewController *loginVC = [[LoginViewController alloc]init];
        [self.navigationController pushViewController:loginVC animated:YES];
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
    if(!isTimerRunning)
        return;
    
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
        
        [[DDSocket currentSocket]sendData:outStr timeOut:-1.0 tag:0];
    }
}
@end
