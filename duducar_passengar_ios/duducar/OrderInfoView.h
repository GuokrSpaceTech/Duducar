//
//  OrderInfoView.h
//  duducar
//
//  Created by wenpeifang on 15/12/12.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HCSStarRatingView.h"
#import "Driver.h"
@interface OrderInfoView : UIView
{
    UIImageView * photoImageView;
    UIView * topView;
    UIView * bottomView;
}
@property (nonatomic,strong)UIImageView * avatorImageView;
@property (nonatomic,strong)UIImageView * picImageView;
@property (nonatomic,strong)HCSStarRatingView * ratingView;
@property (nonatomic,strong)UILabel * driverNameLabel;
@property (nonatomic,strong)UILabel * carNameLabel;
@property (nonatomic,strong)UILabel * carNumLabel;
@property (nonatomic,strong)Driver * driver;
-(void)allViewFrame;
-(void)smallViewFrame;
@end
