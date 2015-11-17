//
//  AppDelegate.h
//  duducar
//
//  Created by mactop on 11/16/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <BaiduMapAPI_Base/BMKBaseComponent.h>
@import CocoaAsyncSocket;

@interface AppDelegate : NSObject <UIApplicationDelegate, BMKGeneralDelegate> {
    UIWindow *window;
    UINavigationController *navigationController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navigationController;

@end

