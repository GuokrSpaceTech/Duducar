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
    GCDAsyncSocket * asyncSocket;
}
+(DDSocket *)currentSocket;
-(void)sendData:(NSData *)data timeOut:(int)time tag:(int)tag;
@end
