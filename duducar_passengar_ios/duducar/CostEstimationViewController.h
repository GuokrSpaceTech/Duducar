//
//  CostEstimationViewController.h
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CostEstimationViewController : UIViewController
@property (weak, nonatomic) IBOutlet UIView *topView;
@property (weak, nonatomic) IBOutlet UILabel *startLocLabel;
@property (weak, nonatomic) IBOutlet UILabel *endLocLabel;
@property (weak, nonatomic) IBOutlet UILabel *CostEstLabel;
@property (weak, nonatomic) IBOutlet UIButton *confirmButton;
- (IBAction)confirmAction:(id)sender;

@end
