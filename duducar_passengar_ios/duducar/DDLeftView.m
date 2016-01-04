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
    UIButton     *mobileLabel;
}
@end

@implementation DDLeftView

-(instancetype)initWithFrame:(CGRect)frame
{
    float BUTTON_WIDTH  = 200-20;
    float BUTTON_HEIGHT = 50;
    float ICON_WIDTH = 20;
    float ICON_HEIGHT = 20;
    float MENU_START_Y = 155;
    float SPACE = 8;
    
    if(self = [super initWithFrame:frame])
    {
        UIView * bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        bgView.backgroundColor = [UIColor colorWithHexString:@"212B37" alpha:1.0f];
        bgView.alpha = 0.8;
        [self addSubview:bgView];
        
        UIView * contentView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 200, frame.size.height)];
        contentView.backgroundColor = [UIColor clearColor];
        [self addSubview:contentView];
        
        //个人信息Header
        UIView *headerView = [[UIView alloc]init];
        avatarImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_account_circle_white"]];
        [headerView addSubview:avatarImageView];
        
        mobileLabel =[UIButton buttonWithType:UIButtonTypeCustom];
        mobileLabel.titleLabel.textColor = [UIColor whiteColor];
        mobileLabel.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        mobileLabel.tag = 99;
        [mobileLabel addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [mobileLabel setTitle:@"点击登录" forState:UIControlStateNormal];
        
        UIView *line = [[UIView alloc]init];
        line.backgroundColor = [UIColor grayColor];
        
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
            make.width.mas_equalTo(@40);
            make.height.mas_equalTo(@40);
            make.top.equalTo(headerView.mas_top).offset(4);
            make.left.equalTo(headerView.mas_left).offset(4);
        }];
        avatarImageView.layer.cornerRadius = 20;
        avatarImageView.clipsToBounds = YES;
        avatarImageView.backgroundColor = [UIColor clearColor];
        avatarImageView.contentMode = UIViewContentModeScaleAspectFit;
        
        [mobileLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_right).offset(0);
            make.centerY.equalTo(avatarImageView.mas_centerY);
            make.right.equalTo(contentView.mas_right).offset(0);
            make.height.mas_equalTo(@(BUTTON_HEIGHT-SPACE));
        }];
        
        [line mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(headerView.mas_left).offset(SPACE);
            make.top.equalTo(headerView.mas_bottom).offset(-SPACE);
            make.height.mas_equalTo(@(1));
            make.width.equalTo(contentView.mas_width).offset(-SPACE);
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
        button2.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft|UIControlContentVerticalAlignmentTop;
        [button2 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button2 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button2.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        [contentView addSubview:button2];
        [button2 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_history_order.mas_right).offset(SPACE);
            make.centerY.equalTo(icon_history_order.mas_centerY);
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
        button3.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft|UIControlContentVerticalAlignmentTop;
        [button3 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button3 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button3.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        [contentView addSubview:button3];
        [button3 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_aboutus.mas_right).offset(SPACE);
            make.centerY.equalTo(icon_aboutus.mas_centerY);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        UIImageView *icon_clause = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_list_white"]];
        [contentView addSubview:icon_clause];
        [icon_clause mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y + BUTTON_HEIGHT*2));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];
        UIButton * button4 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button4 setTitle:@"使用条款" forState:UIControlStateNormal];
        button4.tag = 102;
        button4.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft|UIControlContentVerticalAlignmentTop;
        [button4 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button4 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button4.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        [contentView addSubview:button4];
        [button4 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_clause.mas_right).offset(SPACE);
            make.centerY.equalTo(icon_clause.mas_centerY);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        
        UIImageView *icon_help = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ic_help_outline_white"]];
        [contentView addSubview:icon_help];
        [icon_help mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(avatarImageView.mas_left);
            make.top.mas_equalTo(@(MENU_START_Y + BUTTON_HEIGHT*3));
            make.width.mas_equalTo(@(ICON_WIDTH));
            make.height.mas_equalTo(@(ICON_HEIGHT));
        }];
        UIButton * button5 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button5 setTitle:@"帮助" forState:UIControlStateNormal];
        button5.tag = 103;
        button5.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft|UIControlContentVerticalAlignmentTop;
        [button5 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button5 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button5.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        [contentView addSubview:button5];
        [button5 mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(icon_help.mas_right).offset(SPACE);
            make.centerY.equalTo(icon_help.mas_centerY);
            make.width.mas_equalTo(@(BUTTON_WIDTH));
            make.height.mas_equalTo(@(BUTTON_HEIGHT));
        }];
        
        UIView *divide_line = [[UIView alloc]init];
        [contentView addSubview:divide_line];
        [divide_line mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(headerView.mas_left).offset(SPACE);
            make.top.equalTo(button5.mas_bottom).offset(SPACE);
            make.height.mas_equalTo(@(1));
            make.width.mas_equalTo(contentView.mas_width).offset(-SPACE);
        }];
        
        UIButton * button_exit = [UIButton buttonWithType:UIButtonTypeCustom];
        [button_exit setTitle:@"退出登录" forState:UIControlStateNormal];
        button_exit.tag = 104;
        button_exit.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
        button_exit.titleLabel.font = [UIFont systemFontOfSize:14.0f];
        [button_exit addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button_exit setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button_exit.backgroundColor = [UIColor redColor];
        button_exit.clipsToBounds = YES;
        button_exit.layer.cornerRadius = 5;
        [contentView addSubview:button_exit];
        [button_exit mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(divide_line.mas_bottom).offset(SPACE);
            make.left.equalTo(avatarImageView.mas_left);
            make.height.mas_equalTo(@(BUTTON_HEIGHT-SPACE*2));
            make.width.mas_equalTo(@(BUTTON_WIDTH));
        }];
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
    
    //如果用户登录，获取电话信息，取消点击登录
    [mobileLabel setTitle:mobile forState:UIControlStateNormal];
    [mobileLabel removeTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
}

-(void)buttonClick:(UIButton *)but
{
    NSInteger tag = but.tag;
    if(tag == 100)
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
        // 退出
        [_delegate leftView:self index:4];
    }
    else if(tag == 99)
    {
        //点击登录
        [_delegate leftView:self index:4];
    }
}

@end
