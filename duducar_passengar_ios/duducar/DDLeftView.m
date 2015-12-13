//
//  DDLeftView.m
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "DDLeftView.h"

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
        //个人信息 历史订单
        UIButton * button1 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button1 setTitle:@"个人信息" forState:UIControlStateNormal];
        button1.frame = CGRectMake(10, 75, 200-20, 40);
        button1.tag = 100;
        button1.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button1 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        [button1 setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [contentView addSubview:button1];
        UIButton * button2 = [UIButton buttonWithType:UIButtonTypeCustom];
        [button2 setTitle:@"历史订单" forState:UIControlStateNormal];
        button2.tag = 101;
        button2.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [button2 addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        button2.frame = CGRectMake(10, 75+40, 200-20, 40);
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
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
