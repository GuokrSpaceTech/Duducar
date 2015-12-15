//
//  CostEstimationViewController.m
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "CostEstimationViewController.h"
#import "PaymentViewController.h"
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>
#import "DDSocket.h"
#import "CallCarViewController.h"

@interface CostEstimationViewController ()
{
    BMKRouteSearch* routesearch;
    int distance_max;
    int distance_min;
}
@end

@implementation CostEstimationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([self respondsToSelector:@selector(edgesForExtendedLayout)])
        self.edgesForExtendedLayout = UIRectEdgeNone;
    
    self.startLocLabel.text = _startLoc.name;
    self.endLocLabel.text = _endLoc.name;
    
    //搜索路径
    routesearch = [[BMKRouteSearch alloc]init];
    routesearch.delegate = self;
    BMKPlanNode* start = [[BMKPlanNode alloc]init];
    start.name = _startLoc.name;
    start.cityName =  _city;
    start.pt = _startLoc.coordinate2D;
    BMKPlanNode* end = [[BMKPlanNode alloc]init];
    end.name = _endLoc.name;
    end.cityName =  _city;
    end.pt = _endLoc.coordinate2D;
    
    BMKDrivingRoutePlanOption *drivingRouteSearchOption = [[BMKDrivingRoutePlanOption alloc]init];
    drivingRouteSearchOption.from = start;
    drivingRouteSearchOption.to = end;
    BOOL flag = [routesearch drivingSearch:drivingRouteSearchOption];
    
    if(flag)
    {
        NSLog(@"route search success.");
    }
    else
    {
        NSLog(@"route search failed!");
    }
    
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
#pragma mark
#pragma mark == User Actions
- (IBAction)confirmAction:(id)sender {
    
    [_delegate userConfirmCallCabFrom:_startLoc To:_endLoc];
    
    [self.navigationController popToRootViewControllerAnimated:YES];
}

#pragma mark
#pragma mark == RoutPlan Delegate
-(void)onGetDrivingRouteResult:(BMKRouteSearch *)searcher result:(BMKDrivingRouteResult *)result errorCode:(BMKSearchErrorCode)error
{
    if (error == BMK_SEARCH_NO_ERROR) {
        for(int i=0; i<result.routes.count; i++)
        {
            BMKDrivingRouteLine* plan = (BMKDrivingRouteLine*)[result.routes objectAtIndex:i];
            
            //计算最大和最小距离
            if(i==0)
            {
                distance_max = plan.distance;
                distance_min = plan.distance;
            } else {
               if(plan.distance>distance_max) distance_max = plan.distance;
               if(plan.distance<distance_min) distance_min = plan.distance;
            }
        }
    }
    
    int max_price = distance_max/1000 * 1.2;
    int min_price = distance_min/1000 * 1.2;
    
    NSString *priceString = [NSString stringWithFormat:@"%d - %d", min_price, max_price];
    _CostEstLabel.text = priceString;
}
@end
