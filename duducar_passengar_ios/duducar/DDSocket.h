//
//  DDSocket.h
//  duducar
//
//  Created by wenpeifang on 15/12/8.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GCDAsyncSocket.h"
@interface DDSocket : NSObject
{
    GCDAsyncSocket *asyncSocket;
    NSDictionary   *handlerQueue;
}
+(DDSocket *)currentSocket;
- (void)startSocket;
-(void)sendData:(NSData *)data timeOut:(int)time tag:(int)tag;
-(void)sendCarRequest:(NSDictionary *)paramDict;
-(void)sendLoginRequest:(NSDictionary *)paramDict;

@end
