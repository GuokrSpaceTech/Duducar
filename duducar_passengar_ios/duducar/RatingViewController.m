//
//  RatingViewController.m
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "RatingViewController.h"
#import "UIImageView+WebCache.h"
#import "DDSocket.h"


@interface RatingViewController ()
{
    NSString *avatarUrlString;
    int userRate;
}
@end

static NSString * responseNotificationName = @"DDSocketResponseNotification";

@implementation RatingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //导航条
    self.navigationItem.title = @"评价";
    
    if ([self respondsToSelector:@selector(edgesForExtendedLayout)])
        self.edgesForExtendedLayout = UIRectEdgeNone;
    
    if(_activeOrder)
    {
        NSNumber *price = [_activeOrder objectForKey:@"sumprice"];
        _chargeLabel.text = [NSString stringWithFormat:@"%@", price];
    }
    
    if(_driver)
    {
        _driverNameLabel.text = _driver.name;
        _carDescLabel.text = _driver.carDescription;
        _carPlateNumberLabel.text = _driver.plate;
        [_driverAvatarImageView sd_setImageWithURL:[NSURL URLWithString:_driver.avatar] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
        }];
        _rateingView.value = [_driver.rating floatValue];
        _rateingView.userInteractionEnabled = false;
        _rateLabel.text = [NSString stringWithFormat:@"%@星",_driver.rating];
    }
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(makePhoneCall:)];
    [_phoneImageView addGestureRecognizer:tap];
    
    /*
     * 监听来自Socket的服务消息
     */
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];

}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void)setDriver:(Driver *)driver
{
    _driver = driver;
}

-(void)setActiveOrder:(NSDictionary *)activeOrder
{
    _activeOrder = activeOrder;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)rateValueChanged:(id)sender {
    int rateValue = _ratingViewUserRate.value;
    int orderid = [[_activeOrder objectForKey:@"id"] intValue];
    NSDictionary *paramDict = @{@"cmd":@"comment", @"role":@"2", @"comments":@"", @"order_id":@(orderid), @"rating":@(rateValue)};
    [[DDSocket currentSocket]sendRequest:paramDict];
    
    [self performSelector:@selector(backtoMainController:) withObject:self afterDelay:3];
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
    
    if([command isEqualToString:@"comment_resp"])
    {
        if([status intValue]==1)
        {
            NSLog(@"评价成功");
        } else {
            NSLog(@"评价失败");
        }
        
        UIAlertView *alter = [[UIAlertView alloc] initWithTitle:@"评价" message:@"感谢您的评价！" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        
        [alter show];
    }
}

-(void)backtoMainController:(id)sender
{
    [self.navigationController popToRootViewControllerAnimated:YES];
}

-(void)makePhoneCall:(id)sender
{
    NSString *phoneNumber = [@"tel://" stringByAppendingString:_driver.mobile];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:phoneNumber]];
}
@end
