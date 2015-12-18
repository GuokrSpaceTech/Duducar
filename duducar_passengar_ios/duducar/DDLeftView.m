//
//  DDLeftView.m
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "DDLeftView.h"
#import "Masonry.h"
#import "UIImageView+WebCache.h"

@interface DDLeftView()
{
    UIImageView *avatarImageView;
    UILabel     *mobileLabel;
}
@end

@implementation DDLeftView

-(instancetype)initWithFrame:(CGRect)frame
{
    if(self = [super initWithFrame:frame])
    {
        UIView * bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        bgView.backgroundColor = [UIColor blackColor];
        bgView.alpha = 0.5;
        [self addSubview:bgView];
        
        UIView * contentView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        contentView.backgroundColor = [UIColor clearColor];
        [self addSubview:contentView];
        
        //个人信息Header，历史订单
        UIView *headerView = [[UIView alloc]init];
        avatarImageView = [[UIImageView alloc]init];
        [headerView addSubview:avatarImageView];
        mobileLabel =[[UILabel alloc]init];
        mobileLabel.textColor = [UIColor whiteColor];
        mobileLabel.font = [UIFont systemFontOfSize:14.0f];
        
        [headerView addSubview:avatarImageView];
        [headerView addSubview:mobileLabel];
        [contentView addSubview:headerView];
        
        //Autolayout Constrains
        [headerView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.width.mas_equalTo(@(200-20));
            make.height.mas_equalTo(@(80));
            make.top.equalTo(self).offset(75);
            make.left.equalTo(self).offset(10);
        }];
        
        [avatarImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.width.mas_equalTo(@50);
            make.height.mas_equalTo(@50);
            make.top.equalTo(headerView.mas_top).offset(4);
            make.left.equalTo(headerView.mas_left).offset(4);
        }];
        avatarImageView.layer.cornerRadius = 25;
        avatarImageView.clipsToBounds = YES;
        avatarImageView.backgroundColor = [UIColor whiteColor];
        avatarImageView.contentMode = UIViewContentModeScaleAspectFit;
        
        [mobileLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_right).offset(10);
            make.baseline.equalTo(avatarImageView.mas_baseline);
        }];
        
//        UIButton * button1 = [UIButton buttonWithType:UIButtonTypeCustom];
//        [button1 setTitle:@"个人信息" forState:UIControlStateNormal];
//        button1.frame = CGRectMake(10, 155, 200-20, 40);
//        button1.tag = 100;
//        button1.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
//        [button1 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
//        [button1 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
//        [contentView addSubview:button1];
        UIButton * button2 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button2 setTitle:@"历史订单" forState:UIControlStateNormal];
        button2.tag = 101;
        button2.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button2 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button2.frame = CGRectMake(10, 155+40, 200-20, 40);
        [button2 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button2];
        
    }
    return self;
}
-(void)buttonClick:(UIButton *)but
{
    NSInteger tag = but.tag;
    if(tag == 100)
    {
       // 个人信息
        [_delegate leftView:self index:0];
    }
    else
    {
        [_delegate leftView:self index:1];
    }
}
-(void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [_delegate leftViewClose:self];
}

-(void)setAvatarImage:(NSString*)imageUrl mobile:(NSString *)mobile
{
    [avatarImageView sd_setImageWithURL:[NSURL URLWithString:imageUrl] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
        NSLog(@"User Avatar load Completed.");
    }];
    
    mobileLabel.text = mobile;
}
@end
