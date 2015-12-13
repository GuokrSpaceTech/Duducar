//
//  Driver.h
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Driver : NSObject
@property (nonatomic,strong)NSString * avatar; //头像
@property (nonatomic,strong)NSString * carDescription; //车描述
@property (nonatomic,strong)NSString * mobile; //电话
@property (nonatomic,strong)NSString * name; //司机名称
@property (nonatomic,strong)NSString * picture;//汽车图片
@property (nonatomic,strong)NSString * plate;//车牌号
@property (nonatomic,strong)NSString * rating;//评分

-(instancetype)initWithDic:(NSDictionary *)dic;
@end
