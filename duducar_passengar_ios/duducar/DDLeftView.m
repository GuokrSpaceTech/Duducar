//
//  DDLeftView.m
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "DDLeftView.h"
#import "UIImageView+WebCache.h"
#import "Masonry.h"

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
        button2.tag = 100;
        button2.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button2 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button2.frame = CGRectMake(10, 155, 200-20, 40);
        [button2 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button2];
        
        UIButton * button3 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button3 setTitle:@"关于我们" forState:UIControlStateNormal];
        button3.tag = 101;
        button3.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button3 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button3.frame = CGRectMake(10, 155+40, 200-20, 40);
        [button3 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button3];
        
        UIButton * button4 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button4 setTitle:@"使用条款" forState:UIControlStateNormal];
        button4.tag = 102;
        button4.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button4 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button4.frame = CGRectMake(10, 155+(40*2), 200-20, 40);
        [button4 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button4];
        
        UIButton * button5 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button5 setTitle:@"帮助" forState:UIControlStateNormal];
        button5.tag = 103;
        button5.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button5 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button5.frame = CGRectMake(10, 155+(40*3), 200-20, 40);
        [button5 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button5];
    }
    return self;
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

-(void)buttonClick:(UIButton *)but
{
    NSInteger tag = but.tag;
    if(     tag == 100)
    {
        // 历史订单
        [_delegate leftView:self index:0];
    }
    else if(tag == 101)
    {
        // 关于我们
        [_delegate leftView:self index:1];
        
    }
    else if(tag == 102)
    {
        // 使用条款
        [_delegate leftView:self index:2];
    }
    else if(tag == 103)
    {
        // 帮助
        [_delegate leftView:self index:3];
    }
}

@end
