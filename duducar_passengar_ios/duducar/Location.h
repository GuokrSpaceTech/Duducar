//
//  Location.h
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
@interface Location : NSObject
@property (nonatomic,strong) NSString * name;
@property (nonatomic,assign)CLLocationCoordinate2D  coordinate2D;
@end
