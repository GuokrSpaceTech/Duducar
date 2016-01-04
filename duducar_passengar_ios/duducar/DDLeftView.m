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
#import "UIColor+RCColor.h"

@interface DDLeftView()
{
    UIImageView *avatarImageView;
    UILabel     *mobileLabel;
}
@end

@implementation DDLeftView

-(instancetype)initWithFrame:(CGRect)frame
{
    float BUTTON_WIDTH  = 200-20;
    float BUTTON_HEIGHT = 50;
    float ICON_WIDTH = 30;
    float ICON_HEIGHT = 30;
    float MENU_START_Y = 155;
    float SPACE = 8;
    
    if(self = [super initWithFrame:frame])
    {
        UIView * bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        bgView.backgroundColor = [UIColor colorWithHexString:@"212B37" alpha:1.0f];
        bgView.alpha = 0.3;
        [self addSubview:bgView];
        
        UIView * contentView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        contentView.backgroundColor = [UIColor clearColor];
        [self addSubview:contentView];
        
        //个人信息Header
        UIView *headerView = [[UIView alloc]init];
        avatarImageView = [[UIImageView alloc]init];
        [headerView addSubview:avatarImageView];
        mobileLabel =[[UILabel alloc]init];
        mobileLabel.textColor = [UIColor whiteColor];
        mobileLabel.font = [UIFont systemFontOfSize:14.0f];
        
        UIView *line = [[UIView alloc]init];
        
        [headerView addSubview:avatarImageView];
        [headerView addSubview:mobileLabel];
        [contentView addSubview:headerView];
        [contentView addSubview:line];
        
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
        avatarImageView.backgroundColor = [UIColor clearColor];
        avatarImageView.contentMode = UIViewContentModeScaleAspectFit;
        
        [mobileLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_right).offset(10);
            make.centerY.equalTo(avatarImageView.mas_centerY);
        }];
        
        [line mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(headerView.mas_left).offset(8);
            make.top.equalTo(headerView.mas_bottom);
            make.height.mas_equalTo(@(1));
        }];
        
        UIImageView *icon_history_order = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_history_white"]];
        [contentView addSubview:icon_history_order];
        [icon_history_order mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];

        UIButton * button2 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button2 setTitle:@"历史订单" forState:UIControlStateNormal];
        button2.tag = 100;
        button2.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button2 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button2 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button2];
        [button2 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_history_order.mas_left).offset(SPACE);
            make.top.equalTo(icon_history_order);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        
        UIImageView *icon_aboutus = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_info_outline_white"]];
        [contentView addSubview:icon_aboutus];
        [icon_aboutus mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y + BUTTON_HEIGHT));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];
        UIButton * button3 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button3 setTitle:@"关于我们" forState:UIControlStateNormal];
        button3.tag = 101;
        button3.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button3 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button3 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button3];
        [button3 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_aboutus.mas_left).offset(SPACE);
            make.top.equalTo(icon_aboutus.mas_top);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        UIImageView *icon_clause = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_list_white"]];
        [contentView addSubview:icon_clause];
        [icon_clause mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y + BUTTON_HEIGHT + BUTTON_HEIGHT));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];
        UIButton * button4 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button4 setTitle:@"使用条款" forState:UIControlStateNormal];
        button4.tag = 102;
        button4.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button4 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button4 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button4];
        [button4 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_clause.mas_left).offset(SPACE);
            make.top.equalTo(icon_clause.mas_top);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        
        UIImageView *icon_help = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_help_outline"]];
        [contentView addSubview:icon_help];
        [icon_help mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y + BUTTON_HEIGHT + BUTTON_HEIGHT + BUTTON_HEIGHT));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];
        UIButton * button5 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button5 setTitle:@"帮助" forState:UIControlStateNormal];
        button5.tag = 103;
        button5.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button5 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button5 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button5];
        [button5 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_help.mas_left).offset(SPACE);
            make.top.equalTo(icon_help.mas_top);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        UIView *divide_line = [[UIView alloc]init];
        [contentView addSubview:divide_line];
        [line mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(headerView.mas_left).offset(SPACE);
            make.top.equalTo(button5.mas_bottom).offset(4);
            make.height.mas_equalTo(@(1));
        }];
        
        UIView *logoutBackView = [[UIView alloc]init];
        logoutBackView.backgroundColor = [UIColor redColor];
        [contentView addSubview:logoutBackView];
        [logoutBackView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(contentView.mas_bottom).offset(-SPACE);
            make.left.equalTo(avatarImageView.mas_left);
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
            make.width.equalTo(contentView.mas_width).offset(SPACE);
        }];
        
        UIImageView *logoutIcon = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_power_settings_new_white"]];
        logoutIcon.frame = CGRectMake(10, 5, 30, 30);
        UIButton * button6 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button6 setTitle:@"退出登录" forState:UIControlStateNormal];
        button6.tag = 104;
        button6.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button6 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button6.frame = CGRectMake(10+30+10, 0, 200-50-10, 40);
        [button6 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [logoutBackView addSubview:button6];
        [logoutBackView addSubview:logoutIcon];
        
        
    }
    return self;
}

-(void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [_delegate leftViewClose:self];
}

-(void)setAvatarImage:(NSString*)imageUrl mobile:(NSString *)mobile
{
//    [avatarImageView sd_setImageWithURL:[NSURL URLWithString:imageUrl] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
//        NSLog(@"User Avatar load Completed.");
//    }];
    
    [avatarImageView setImage:[UIImage imageNamed:@"ic_account_circle_white"]];
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
    else if(tag == 104)
    {
        // 帮助
        [_delegate leftView:self index:4];
    }
}

@end
