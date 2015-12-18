//
//  PaymentViewController.h
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PaymentViewController : UIViewController

- (IBAction)paymentAction:(id)sender;
- (IBAction)aliPaySelected:(id)sender;
- (IBAction)WechatPaySelected:(id)sender;

@property (weak, nonatomic) IBOutlet UILabel *chargeLabel;
@property (strong, nonatomic) NSDictionary *activeOrder;

@property (nonatomic, strong) NSString *chargePrice;
@property (nonatomic, strong) NSString *mileage;
@property (nonatomic, strong) NSString *lowSpeedTime;
@end
