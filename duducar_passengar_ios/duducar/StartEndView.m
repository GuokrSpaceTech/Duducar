//
//  StartEndView.m
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "StartEndView.h"
#import "Masonry.h"

@implementation StartEndView
-(instancetype)initWithFrame:(CGRect)frame
{
    if(self = [super initWithFrame:frame])
    {
        self.backgroundColor = [UIColor whiteColor];
        
        UIImageView * lineImageView = [[UIImageView alloc]initWithFrame:CGRectMake(5, frame.size.height/2.0-1, frame.size.width-10, 1)];
        lineImageView.backgroundColor = [UIColor colorWithRed:51/255.0 green:51/255.0 blue:51/255.0 alpha:0.7f];
        [self addSubview:lineImageView];
        
        UIImageView * greenImageView = [[UIImageView alloc]init];
        greenImageView.image = [UIImage imageNamed:@"startIndicator"];
        greenImageView.contentMode=UIViewContentModeScaleToFill;
        [self addSubview:greenImageView];
        
        UIImageView * redImageView = [[UIImageView alloc]init];
        redImageView.image = [UIImage imageNamed:@"endIndicator"];
        redImageView.contentMode=UIViewContentModeScaleToFill;
        [self addSubview:redImageView];
        
        [greenImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.mas_left).offset(8);
            make.top.equalTo(self.mas_top).offset(16);
            make.height.mas_equalTo(self.frame.size.height/2-16);
            make.width.mas_equalTo(6);
        }];
        
        [redImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.mas_left).offset(8);
            make.top.equalTo(greenImageView.mas_bottom);
            make.height.mas_equalTo(self.frame.size.height/2-16);
            make.width.mas_equalTo(6);
        }];
        
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
