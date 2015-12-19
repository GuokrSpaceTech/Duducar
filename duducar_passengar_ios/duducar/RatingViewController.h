//
//  RatingViewController.h
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HCSStarRatingView.h"
#import "Driver.h"

@interface RatingViewController : UIViewController

@property (weak, nonatomic) IBOutlet HCSStarRatingView *rateingView;
@property (weak, nonatomic) IBOutlet HCSStarRatingView *ratingViewUserRate;
@property (weak, nonatomic) IBOutlet UIImageView *driverAvatarImageView;
@property (weak, nonatomic) IBOutlet UILabel *driverNameLabel;
@property (weak, nonatomic) IBOutlet UILabel *carDescLabel;
@property (weak, nonatomic) IBOutlet UILabel *carPlateNumberLabel;
@property (weak, nonatomic) IBOutlet UILabel *chargeLabel;
@property (weak, nonatomic) IBOutlet UILabel *rateLabel;

@property (strong, nonatomic) Driver *driver;
@property (strong,nonatomic) NSDictionary *activeOrder;
- (IBAction)rateValueChanged:(id)sender;
@end
