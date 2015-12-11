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
#import "Masonry.h"
#import "DDSocket.h"
@interface DDMainViewController ()<BMKMapViewDelegate,BMKLocationServiceDelegate,BMKGeoCodeSearchDelegate>
{
    BMKGeoCodeSearch *_geocodesearch;;
    
    UIButton * startButton;
    UIButton * stopButton;
    
    
    CLLocationCoordinate2D startCoordinate2D;
    CLLocationCoordinate2D stopCoordinate2D;
    
}
@property (nonatomic,strong)BMKMapView* mapView ;
@property (nonatomic,strong)BMKLocationService *locService;
@end

@implementation DDMainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(socketData:) name:@"DDSocketResponseNotification" object:nil];
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
    
    
    
    UIView * topView = [[UIView alloc]initWithFrame:CGRectMake(10, self.view.frame.size.height - 170, self.view.frame.size.width-20, 100)];
    topView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:topView];
    
 
    startButton = [UIButton buttonWithType:UIButtonTypeCustom];
    startButton.frame = CGRectMake(0, 0, topView.frame.size.width, topView.frame.size.height/2.0);
    [startButton setTitle:@"输入起点" forState:UIControlStateNormal];
    [startButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [startButton addTarget:self action:@selector(start:) forControlEvents:UIControlEventTouchUpInside];
    [topView addSubview:startButton];
    
    stopButton = [UIButton buttonWithType:UIButtonTypeCustom];
    stopButton.frame = CGRectMake(0, topView.frame.size.height/2.0, topView.frame.size.width, topView.frame.size.height/2.0);
    [stopButton setTitle:@"输入终点" forState:UIControlStateNormal];
    [stopButton addTarget:self action:@selector(stop:) forControlEvents:UIControlEventTouchUpInside];
    [stopButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [topView addSubview:stopButton];
    
    
    UIButton * callCarButton = [UIButton buttonWithType:UIButtonTypeSystem];
    callCarButton.frame = CGRectMake(10, self.view.frame.size.height - 60, self.view.frame.size.width-20, 40);
    callCarButton.backgroundColor = [UIColor blueColor];
    [callCarButton setTitle:@"呼叫专车" forState:UIControlStateNormal];
    [callCarButton addTarget:self action:@selector(callCar:) forControlEvents:UIControlEventTouchUpInside];
    [callCarButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.view addSubview:callCarButton];


    // 叫车大头针
    UIView * view1 = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 10, 10)];
    view1.backgroundColor = [UIColor redColor];
    view1.center = self.view.center;
    [self.view addSubview:view1];
    
    

}
-(void)socketData:(NSNotification *)notification
{
    NSDictionary * responseDict = notification.userInfo;
    NSDictionary * dic11 = @{@"lat":@(39.915),@"lng":@"116.404"};
    responseDict = @{@"cmd":@"get_near_car",@"status":@"1",@"cars":@[dic11]};
    
    if(responseDict)
    {
        NSString *command = [responseDict objectForKey:@"cmd"];
        NSNumber *status =[responseDict objectForKey:@"status"];
        if([command isEqualToString:@"get_near_car"])
        {
            if([status intValue] == 1)
            {
                //获取附近车辆信息
                [_mapView removeAnnotations:_mapView.annotations];
                NSString * car_number = [NSString stringWithFormat:@"%@",responseDict[@"car_number"]];
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
    
}
-(void)callCar:(id)sender
{
    
}
-(void)start:(id)sender
{
    
}
-(void)stop:(id)sender
{
    
}
- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    CGPoint centerPosition = self.view.center;
    CLLocationCoordinate2D  coord = [_mapView convertPoint:centerPosition toCoordinateFromView:self.view];
    
    startCoordinate2D = coord;
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

    
    NSDictionary *postDictionary = [NSDictionary dictionaryWithObjects:@[@"get_near_car",@(coord.latitude),@(coord.longitude),@"1"] forKeys:@[@"cmd",@"lat",@"lng",@"car_type"]];
    
    NSError * error = nil;
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
    
    NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [jsonString appendString:@"\n"];
    NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    //        [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
    [[DDSocket currentSocket]sendData:outStr timeOut:-1.0 tag:0];
    
    
    // 获得当前地理位置
    // 获得最近的车辆
    
}
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
        [startButton setTitle:address forState:UIControlStateNormal];

    }
}
- (void)mapViewDidFinishLoading:(BMKMapView *)mapView
{
    CLLocationCoordinate2D  coord =_locService.userLocation.location.coordinate;
    _mapView.centerCoordinate = coord;
 
}
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
