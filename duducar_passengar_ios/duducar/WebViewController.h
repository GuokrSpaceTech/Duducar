//
//  WebViewController.h
//  duducar
//
//  Created by macbook on 15/12/20.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import </usr/include/Availability.h>  
#import <WebKit/WebKit.h>
@interface WebViewController : UIViewController

#define iOS8 ([[UIDevice currentDevice].systemVersion doubleValue] >= 8.0)
#define RGBACOLOR(r,g,b,a) [UIColor colorWithRed:(r)/255.0f green:(g)/255.0f blue:(b)/255.0f alpha:(a)]
#define SCREENWIDTH [UIScreen mainScreen].bounds.size.width
#define SCREENHEIGHT [UIScreen mainScreen].bounds.size.height

@property (nonatomic,strong)UIWebView * webview;
@property (nonatomic,strong)WKWebView * wkWebview;
@property (nonatomic,copy) NSString * urlStr;
@property (nonatomic,copy)NSString * titleStr;
@end
