//
//  CallCarViewController.h
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DDSocket.h"
#import "Location.h"
@interface CallCarViewController : UIViewController
@property (nonatomic,strong)Location * startLocation;
@property (nonatomic,strong)Location * endLocation;
@end
