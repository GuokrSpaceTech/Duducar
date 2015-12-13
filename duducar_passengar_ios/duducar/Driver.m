//
//  Driver.m
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "Driver.h"

@implementation Driver
-(instancetype)initWithDic:(NSDictionary *)dic
{
    if(self = [super init])
    {
        _mobile = [NSString stringWithFormat:@"%@",dic[@"mobile"]];
        _avatar = [NSString stringWithFormat:@"%@",dic[@"avatar"]];
        _rating = [NSString stringWithFormat:@"%@",dic[@"rating"]];
        _plate = [NSString stringWithFormat:@"%@",dic[@"plate"]];
        _picture = [NSString stringWithFormat:@"%@",dic[@"picture"]];
         _carDescription = [NSString stringWithFormat:@"%@",dic[@"description"]];
        _name = [NSString stringWithFormat:@"%@",dic[@"name"]];
        
    }
    return self;
}
@end
