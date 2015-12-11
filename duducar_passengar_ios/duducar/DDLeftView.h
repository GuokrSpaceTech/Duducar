//
//  DDLeftView.h
//  duducar
//
//  Created by HELLO  on 15/12/11.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
@protocol LeftViewDelegate;
@interface DDLeftView : UIView
@property (nonatomic,weak)id<LeftViewDelegate>delegate;
@end


@protocol LeftViewDelegate <NSObject>

-(void)leftView:(DDLeftView *)leftView index:(NSInteger)index;
-(void)leftViewClose:(DDLeftView *)leftView;

@end