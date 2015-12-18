//
//  HistoryViewController.m
//  duducar
//
//  Created by macbook on 15/12/17.
//  Copyright © 2015年 guokrspace. All rights reserved.
//

#import "HistoryViewController.h"
#import "DDSocket.h"
#import "HistoryTableViewCell.h"
#import "DDDatabase.h"

@interface HistoryViewController ()
{
}
@property (nonatomic,strong) NSMutableArray *orderArray;

@end

static NSString * responseNotificationName = @"DDSocketResponseNotification";

@implementation HistoryViewController

-(void)viewDidLoad {
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
    
    //从数据库里读取数据
    _orderArray = [[NSMutableArray alloc]init];
    [[DDDatabase sharedDatabase]selectOrderHistory:^(NSArray *orders) {
        NSMutableArray *orderList = [NSMutableArray arrayWithArray:orders];
        
        for(NSString *order in orderList)
        {
            //Json to Dict
            NSError *jsonError;
            NSData *objectData = [order dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *orderDict = [NSJSONSerialization JSONObjectWithData:objectData
                                                                    options:NSJSONReadingMutableContainers
                                                                       error:&jsonError];
            
            if (!orderDict) {
                NSLog(@"Json Serilisation: error: %@", jsonError.localizedDescription);
            } else {
                [_orderArray addObject:orderDict];
            }
        }
    }];
    
    //从网络读取
    NSDictionary *paramDict = @{@"cmd":@"history_orders", @"role":@"2", @"type":@"old", @"number":@"10", @"order_id":@LONG_MAX};
    [[DDSocket currentSocket] sendRequest:paramDict];
    
    
    [self.tableView registerNib:[UINib nibWithNibName:@"HistoryTableViewCell" bundle:nil] forCellReuseIdentifier:@"ordercell"];

}

-(void)viewDidAppear:(BOOL)animated
{
}

-(void)viewDidDisappear:(BOOL)animated
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _orderArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    HistoryTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ordercell" forIndexPath:indexPath];
    
    NSDictionary *orderDict = _orderArray[indexPath.row];
    
    cell.startLabel.text = [orderDict objectForKey:@"start"];
    cell.destLabel.text = [orderDict objectForKey:@"destination"];
    cell.dateLabel.text = [NSString stringWithFormat:@"%ld",(long)[orderDict objectForKey:@"start_time"]];
    cell.orderStatusLabel.text = [NSString stringWithFormat:@"%d",(int)[orderDict objectForKey:@"status"]];
    
    return cell;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 150;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here, for example:
    // Create the next view controller.
    <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:<#@"Nib name"#> bundle:nil];
    
    // Pass the selected object to the new view controller.
    
    // Push the view controller.
    [self.navigationController pushViewController:detailViewController animated:YES];
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark
#pragma mark == Receive Notification from Socket Response data
- (void)receiveResponseHandles:(NSNotification *)notification
{
    NSDictionary *responseDict = notification.userInfo;
    
    if(!responseDict)
        return;
    
    NSString *command = [responseDict objectForKey:@"cmd"];
    NSNumber *status = [responseDict objectForKey:@"status"];
    
    if([command isEqualToString:@"history_orders_resp"])
    {
        if([status intValue]==1)
        {
            NSMutableArray *orderList = [responseDict objectForKey:@"order_list"];
            
            //本地数据与远程数据合并
            [orderList addObjectsFromArray:_orderArray];
            [_orderArray removeAllObjects];
            [_orderArray addObjectsFromArray:orderList];
            
            //存入数据库，最多存10条
            int i=0;
            for(NSDictionary *order in _orderArray)
            {
                if(i==10)
                    break;
                
                //dict转jsonData
                NSError *error;
                NSData *jsonData = [NSJSONSerialization dataWithJSONObject:order options:NSJSONWritingPrettyPrinted error:&error];
                if(!error)
                {
                    NSString *jsonStr = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
                    [[DDDatabase sharedDatabase]insertOrder:jsonStr];
                }
                
                i++;
            }
            [self.tableView reloadData];
        }
        else
        {
            NSLog(@"HISTORY_ORDER REQ FAILED.");
        }
    }
}
@end
