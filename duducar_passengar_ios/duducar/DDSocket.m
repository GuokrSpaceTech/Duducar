//
//  DDSocket.m
//  duducar
//
//  Created by wenpeifang on 15/12/8.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "DDSocket.h"
#import "DDLog.h"
#import "DDTTYLogger.h"
#import "DDDispatchQueueLogFormatter.h"
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
#define  SERVER_PORT 8282  // 0 => automatic
#define  SERVER_HOST @"120.24.237.15"

#define USE_SECURE_CONNECTION    0
#define USE_CFSTREAM_FOR_TLS     0 // Use old-school CFStream style technique
#define MANUALLY_EVALUATE_TRUST  0

#define READ_HEADER_LINE_BY_LINE 1


static DDSocket * instanceSocket = nil;
static NSString * responseNotificationName = @"DDSocketResponseNotification";

@implementation DDSocket
+(DDSocket *)currentSocket
{
    if(instanceSocket == nil)
    {
        instanceSocket = [[self alloc]init];
        
    }
    
    return instanceSocket;
}
-(instancetype)init
{
    if(self = [super init])
    {
        DDDispatchQueueLogFormatter *formatter = [[DDDispatchQueueLogFormatter alloc] init];
        [formatter setReplacementString:@"socket" forQueueLabel:GCDAsyncSocketQueueName];
        [formatter setReplacementString:@"socket-cf" forQueueLabel:GCDAsyncSocketThreadName];
        
        [[DDTTYLogger sharedInstance] setLogFormatter:formatter];
        [DDLog addLogger:[DDTTYLogger sharedInstance]];

    }
    return self;
}

- (void)startSocket
{
    asyncSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
    
    NSError *error = nil;
    
    uint16_t port = SERVER_PORT;
    if (port == 0)
    {
#if USE_SECURE_CONNECTION
        port = 443; // HTTPS
#else
        port = 80;  // HTTP
#endif
    }
    
    if (![asyncSocket connectToHost:SERVER_HOST onPort:port error:&error])
    {
        DDLogError(@"Unable to connect to due to invalid configuration: %@", error);
    }
    else
    {
        DDLogVerbose(@"Connecting to \"%@\" on port %hu...", SERVER_HOST, port);
    }
    
#if USE_SECURE_CONNECTION

    
#if USE_CFSTREAM_FOR_TLS
    {
        // Use old-school CFStream style technique
        
        NSDictionary *options = @{
                                  GCDAsyncSocketUseCFStreamForTLS : @(YES),
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#elif MANUALLY_EVALUATE_TRUST
    {
        // Use socket:didReceiveTrust:completionHandler: delegate method for manual trust evaluation
        
        NSDictionary *options = @{
                                  GCDAsyncSocketManuallyEvaluateTrust : @(YES),
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#else
    {
        // Use default trust evaluation, and provide basic security parameters
        
        NSDictionary *options = @{
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#endif
    
#endif
}
-(void)sendData:(NSData *)data timeOut:(int)time tag:(int)tag
{
    [asyncSocket writeData:data withTimeout:time tag:tag];
}

- (void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(UInt16)port
{
    DDLogVerbose(@"socket:didConnectToHost:%@ port:%hu", host, port);
    
    [asyncSocket readDataToData:[GCDAsyncSocket LFData] withTimeout:-1.0 tag:0];
    
}

- (void)socket:(GCDAsyncSocket *)sock didReceiveTrust:(SecTrustRef)trust
completionHandler:(void (^)(BOOL shouldTrustPeer))completionHandler
{
    DDLogVerbose(@"socket:shouldTrustPeer:");
    
    dispatch_queue_t bgQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(bgQueue, ^{
        
        // This is where you would (eventually) invoke SecTrustEvaluate.
        // Presumably, if you're using manual trust evaluation, you're likely doing extra stuff here.
        // For example, allowing a specific self-signed certificate that is known to the app.
        
        SecTrustResultType result = kSecTrustResultDeny;
        OSStatus status = SecTrustEvaluate(trust, &result);
        
        if (status == noErr && (result == kSecTrustResultProceed || result == kSecTrustResultUnspecified)) {
            completionHandler(YES);
        }
        else {
            completionHandler(NO);
        }
    });
}

- (void)socketDidSecure:(GCDAsyncSocket *)sock
{
    // This method will be called if USE_SECURE_CONNECTION is set
    
    DDLogVerbose(@"socketDidSecure:");
}

- (void)socket:(GCDAsyncSocket *)sock didWriteDataWithTag:(long)tag
{
    DDLogVerbose(@"socket:didWriteDataWithTag:");
}

- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
    DDLogVerbose(@"socket:didReadData:withTag:");
    
    [asyncSocket readDataWithTimeout:-1 tag:0];
    
    NSString *response = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    
    //Deserialastion a Json String into Dictionary
    NSError *jsonError;
    NSData  *objectData = [response dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *responseDict = [NSJSONSerialization JSONObjectWithData:objectData
                                                                 options:NSJSONReadingMutableContainers
                                                                   error:&jsonError];
    [[NSNotificationCenter defaultCenter] postNotificationName:responseNotificationName object:self userInfo:responseDict];
    
    DDLogInfo(@"Response:\n%@", response);
    
}

- (void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err
{
    //
    
    DDLogVerbose(@"socketDidDisconnect:withError: \"%@\"", err);
    
//    [self startSocket];
}

#pragma mark
#pragma mark == Protocols
-(void)sendCarRequest:(NSDictionary *)paramDict
{
    NSError * error = nil;
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:paramDict options:NSUTF8StringEncoding error:&error];
    
    NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [jsonString appendString:@"\n"];
    
    NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
}

-(void)sendLoginRequest:(NSDictionary *)paramDict
{
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:paramDict options:NSUTF8StringEncoding error:&error];
    
    NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [jsonString appendString:@"\n"];
    
    NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
}

@end
