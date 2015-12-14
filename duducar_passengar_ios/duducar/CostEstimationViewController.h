//
//  CostEstimationViewController.h
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Location.h"
#import <BaiduMapAPI_Search/BMKSearchComponent.h>

@protocol CostEstimateDelegate <NSObject>
@required

-(void) userConfirmCallCabFrom:(Location *)start To:(Location *)dest;
@end

@interface CostEstimationViewController : UIViewController <BMKRouteSearchDelegate>
@property (weak, nonatomic) IBOutlet UIView *topView;
@property (weak, nonatomic) IBOutlet UILabel *startLocLabel;
@property (weak, nonatomic) IBOutlet UILabel *endLocLabel;
@property (weak, nonatomic) IBOutlet UILabel *CostEstLabel;
@property (weak, nonatomic) IBOutlet UIButton *confirmButton;
@property (nonatomic,strong) Location *startLoc;
@property (nonatomic,strong) Location *endLoc;
@property (nonatomic,strong) NSString *city;
- (IBAction)confirmAction:(id)sender;

@property (nonatomic,weak)   id<CostEstimateDelegate> delegate;

@end
