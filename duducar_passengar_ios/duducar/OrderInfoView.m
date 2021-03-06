//
//  OrderInfoView.m
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "OrderInfoView.h"
#import "UIImageView+WebCache.h"
@implementation OrderInfoView
-(instancetype)initWithFrame:(CGRect)frame
{
    if(self = [super initWithFrame:frame])
    {
        topView = [[UIView alloc]init];
        topView.backgroundColor = [UIColor whiteColor];
        [self addSubview:topView];
        
        bottomView = [[UIView alloc]init];
        bottomView.backgroundColor = [UIColor whiteColor];
        [self addSubview:bottomView];
        
        //初始化UI
        _avatorImageView = [[UIImageView alloc]init];
        [self addSubview:_avatorImageView];
        _avatorImageView.layer.cornerRadius = 30;
        _avatorImageView.backgroundColor = [UIColor whiteColor];
        _avatorImageView.clipsToBounds = YES;
        _picImageView = [[UIImageView alloc]init];
        _picImageView.layer.cornerRadius = 30;
        _picImageView.backgroundColor = [UIColor redColor];
        _picImageView.clipsToBounds = YES;
        [self addSubview:_picImageView];
        
        _ratingView = [[HCSStarRatingView alloc]init];
        _ratingView.userInteractionEnabled = NO;
        _ratingView.value = 3;
        [self addSubview:_ratingView];
        
        _driverNameLabel = [[UILabel alloc]init];
        _driverNameLabel.text = @"xxxxx";
        _driverNameLabel.textColor = [UIColor blackColor];
        _driverNameLabel.textAlignment = NSTextAlignmentLeft;
        [self addSubview:_driverNameLabel];
        
        _carNameLabel = [[UILabel alloc]init];
        _carNameLabel.text = @"xxxx";
        _carNameLabel.textAlignment = NSTextAlignmentCenter;
        _carNameLabel.textColor = [UIColor blackColor];
        [self addSubview:_carNameLabel];
        
        _carNumLabel = [[UILabel alloc]init];
        _carNumLabel.text = @"xxxx";
        _carNumLabel.textAlignment = NSTextAlignmentCenter;
        _carNumLabel.textColor = [UIColor blackColor];
        [self addSubview:_carNumLabel];
        
        photoImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"phoneicon"]];
        photoImageView.contentMode = UIViewContentModeScaleAspectFit;
        [photoImageView setUserInteractionEnabled:YES];
        UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(makePhoneCall:)];
        [photoImageView addGestureRecognizer:tapGesture];
        [self addSubview:photoImageView];
    }
    return self;
}

-(void)smallViewFrame
{
    _picImageView.frame =CGRectZero;
    _avatorImageView.frame = CGRectMake(self.frame.size.width/2.0-30, 0, 60, 60);
    topView.frame = CGRectMake(0, 30, self.frame.size.width, self.frame.size.height);
    _ratingView.frame = CGRectMake(80, 70, self.frame.size.width-160, 30);
    _driverNameLabel.frame = CGRectMake(60, self.frame.size.height-30, 100, 20);
    photoImageView.frame = CGRectMake(self.frame.size.width-90, self.frame.size.height-40, 30, 30);
    bottomView.frame = CGRectMake(0, self.frame.size.height, self.frame.size.width, 1);
    _carNumLabel.frame = CGRectZero;
    _carNameLabel.frame = CGRectZero;
}

-(void)allViewFrame
{
    _avatorImageView.frame = CGRectMake(30, 0, 60, 60);
    _picImageView.frame = CGRectMake(self.frame.size.width-60-30, 0, 60, 60);
    topView.frame = CGRectMake(0, 30, self.frame.size.width, self.frame.size.height-30-50);
    _ratingView.frame = CGRectMake(80, 70, self.frame.size.width-160, 30);
    _driverNameLabel.frame = CGRectMake(30, topView.frame.origin.y+topView.frame.size.height-25, 100, 20);
    _carNameLabel.frame = CGRectMake(self.frame.size.width-100-10, topView.frame.origin.y+topView.frame.size.height-25, 100, 20);
    bottomView.frame = CGRectMake(0, self.frame.size.height-50, self.frame.size.width, 50);
    photoImageView.frame = CGRectMake(35, self.frame.size.height - 40, 30, 30);
    _carNumLabel.frame = CGRectMake(self.frame.size.width-160,self.frame.size.height-50+15 , 150, 20);
}

-(void)setDriver:(Driver *)driver
{
    _driver = driver;
    [_avatorImageView sd_setImageWithURL:[NSURL URLWithString:_driver.avatar] placeholderImage:nil];
    [_picImageView sd_setImageWithURL:[NSURL URLWithString:_driver.picture] placeholderImage:nil];
    _driverNameLabel.text = _driver.name;
    _carNameLabel.text = _driver.carDescription;
    _carNumLabel.text = [NSString stringWithFormat:@"车牌号：%@",_driver.plate];
    _ratingView.value = [_driver.rating intValue];
}
-(void)makePhoneCall:(id)sender
{
    NSString *phoneNumber = [@"tel://" stringByAppendingString:_driver.mobile];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:phoneNumber]];
}
@end
