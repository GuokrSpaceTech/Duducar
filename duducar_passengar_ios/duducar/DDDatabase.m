//
//  DDDatabase.m
//  duducar
//
//  Created by mactop on 12/10/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "DDDatabase.h"

@implementation DDDatabase

-(NSString *)datebasePath
{
    NSArray * paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    return [[paths objectAtIndex:0] stringByAppendingPathComponent:@"Duducar.sqlite"];
}
-(id) init
{
    self = [super init];
    if(self){
        NSString *dbFilePath = [self datebasePath];
        NSLog(@"db_ _ _ _ _%@",dbFilePath);
        queue = [FMDatabaseQueue databaseQueueWithPath:dbFilePath];
        
        [self createTable];
    }
    return self;
}
-(void)createTable
{
    [queue inDatabase:^(FMDatabase *db) {
        NSString *sqlStr = @"CREATE TABLE IF NOT EXISTS personInfo('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,'token' text,'phone' text);";
        [db executeUpdate:sqlStr];
        sqlStr = @"CREATE TABLE IF NOT EXISTS orderHistory('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,'order' text)";
        [db executeUpdate:sqlStr];
    }];
}
-(void)clearTable
{
    [queue inDatabase:^(FMDatabase *db) {
        NSString *sqlStr = @"delete from personInfo;";
        [db executeUpdate:sqlStr];
        sqlStr = @"delete from orderHistory;";
        [db executeUpdate:sqlStr];
    }];
}
-(void)insertDataToPersonInfoTableToken:(NSString *)token phone:(NSString *)phone
{
    [queue inDatabase:^(FMDatabase *db) {
        [db executeUpdate:@"delete  from personInfo"];
        [db executeUpdate:@"insert into personInfo(token,phone) values(?,?)",token,phone];
    }];
}
-(void)selectFromPersonInfo:(void (^)(NSString *token, NSString *phone))completionHandler
{
    [queue inDatabase:^(FMDatabase *db) {
        NSString *token, *phone;
        FMResultSet * set = [db executeQuery:@"select * from personInfo limit 1"];
        while ([set next]) {
            token = [set stringForColumn:@"token"];
            phone = [set stringForColumn:@"phone"];
        }
        completionHandler(token,phone);
    }];
}
-(void)insertOrder:(NSString *)order
{
    [queue inDatabase:^(FMDatabase *db) {
        [db executeUpdate:@"insert into orderHistory('order') values(?)",order];
    }];
}
-(void)selectOrderHistory:(void (^)(NSArray *))completionHandler
{
    [queue inDatabase:^(FMDatabase *db) {
        FMResultSet *set = [db executeQuery:@"select * from orderHistory"];
        NSMutableArray *orderArray = [[NSMutableArray alloc] init];
        while ([set next]) {
            [orderArray addObject:[set stringForColumn:@"order"]];
        }
        completionHandler(orderArray);
    }];
}

+(DDDatabase *) sharedDatabase
{
    
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
    });
    
    return _sharedObject;
}

@end
