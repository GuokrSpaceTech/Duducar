//
//  DDSearchTableViewController.h
//  duducar
//
//  Created by mactop on 12/9/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>

@interface DDSearchTableViewController : UITableViewController

@property (nonatomic,assign) NSString *currCity;
@property (nonatomic,copy) void (^startPointCompletionHandler)(BMKPoiInfo *result);
@property (nonatomic,copy) void (^endPointCompletionHandler)(BMKPoiInfo *result);
@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;

@end
