//
//  StartEndView.m
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "StartEndView.h"

@implementation StartEndView
-(instancetype)initWithFrame:(CGRect)frame
{
    if(self = [super initWithFrame:frame])
    {
        self.backgroundColor = [UIColor whiteColor];
        
        UIImageView * lineImageView = [[UIImageView alloc]initWithFrame:CGRectMake(5, frame.size.height/2.0-1, frame.size.width-10, 1)];
        lineImageView.backgroundColor = [UIColor colorWithRed:51/255.0 green:51/255.0 blue:51/255.0 alpha:1];
        [self addSubview:lineImageView];
        
        
        UIImageView * greenImageView = [[UIImageView alloc]initWithFrame:CGRectMake(10, 5,15, frame.size.height/2.0-5)];
        greenImageView.image = [UIImage imageNamed:@"startIndicator"];
        [self addSubview:greenImageView];
        
        UIImageView * redImageView = [[UIImageView alloc]initWithFrame:CGRectMake(10,frame.size.height/2.0,15, frame.size.height/2.0-5)];
        redImageView.image = [UIImage imageNamed:@"endIndicator"];
        [self addSubview:redImageView];
        
        
        _startLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, 0, frame.size.width-50, frame.size.height/2.0)];
        _startLabel.backgroundColor = [UIColor clearColor];
        [self addSubview:_startLabel];
        
        _endLabel = [[UILabel alloc]initWithFrame:CGRectMake(40, frame.size.height/2.0, frame.size.width-50, frame.size.height/2.0)];
        _endLabel.backgroundColor = [UIColor clearColor];
        [self addSubview:_endLabel];
    }
    return self;
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
