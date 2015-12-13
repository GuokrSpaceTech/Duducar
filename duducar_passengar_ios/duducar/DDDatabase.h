//
//  DDDatabase.h
//  duducar
//
//  Created by mactop on 12/10/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDB.h"

@interface DDDatabase : NSObject
{
    FMDatabaseQueue *queue;
}

+(DDDatabase *)sharedDatabase;
-(void)clearTable;
-(void)createTable;
-(void)selectFromPersonInfo:(void (^)(NSString *token, NSString *phone))completionHandler;
-(void)insertDataToPersonInfoTableToken:(NSString *)token phone:(NSString *)phone;
@end
