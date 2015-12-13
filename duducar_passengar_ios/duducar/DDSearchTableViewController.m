//
//  DDSearchTableViewController.m
//  duducar
//
//  Created by mactop on 12/9/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "DDSearchTableViewController.h"
#import "SearchResultTableViewCell.h"
#import <BaiduMapAPI_Search/BMKSearchComponent.h>
#import "CostEstimationViewController.h"
#import "Masonry.h"

@interface DDSearchTableViewController () <UISearchBarDelegate,UISearchControllerDelegate, UISearchDisplayDelegate, BMKPoiSearchDelegate>
{
    NSMutableArray *searchResult;
    BMKPoiSearch   *poisearch;
    int curPage;
    NSString *searchKeyWord;

}
@property (nonatomic,strong) UISearchController *searchController;
@end

static NSString *cellidentify = @"resultItem";

@implementation DDSearchTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //SearchBar is initialised by XIB
    
    //Tableview
    [self.tableView registerClass:[SearchResultTableViewCell class] forCellReuseIdentifier:cellidentify];
    self.tableView.separatorColor = UITableViewCellSeparatorStyleNone;
    
    //PoiSearch Init
    searchResult = [[NSMutableArray alloc] init];
    poisearch = [[BMKPoiSearch alloc]init];
    poisearch.delegate = self;
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
        return searchResult.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    SearchResultTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellidentify forIndexPath:indexPath];
    
    BMKPoiInfo *poiInfo = searchResult[indexPath.row];
    
    [cell setCellContentAddrName:poiInfo.name withAddrDetail:poiInfo.address];
    
    return cell;
}

#pragma mark - Table view Delegate
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    BMKPoiInfo *selectedAddr = searchResult[indexPath.row];
    
    if(_startPointCompletionHandler!=nil)
    {
        _startPointCompletionHandler(selectedAddr);
        [self.navigationController popViewControllerAnimated:YES];
    } else if(_endPointCompletionHandler!=nil) {
        _endPointCompletionHandler(selectedAddr);
        [self.navigationController popViewControllerAnimated:YES];
//        CostEstimationViewController *costVC = [[CostEstimationViewController alloc]initWithNibName:@"CostEstimationViewController" bundle:nil];
//        [self.navigationController pushViewController:costVC animated:YES];
    }
}

#pragma mark -
#pragma mark implement BMKSearchDelegate
- (void)onGetPoiResult:(BMKPoiSearch *)searcher result:(BMKPoiResult*)result errorCode:(BMKSearchErrorCode)error
{
    if (error == BMK_SEARCH_NO_ERROR) {
        [searchResult removeAllObjects];
        [searchResult addObjectsFromArray:result.poiInfoList];
        
        [self.tableView reloadData];
    } else if (error == BMK_SEARCH_AMBIGUOUS_ROURE_ADDR){
        NSLog(@"起始点有歧义");
    } else {
        // 各种情况的判断。。。
    }
}

#pragma mark -
#pragma mark - implement UISearchBar Delegate
-(void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
{
    //Start Search
    curPage = 0;
    BMKCitySearchOption *citySearchOption = [[BMKCitySearchOption alloc]init];
    citySearchOption.pageIndex = curPage;
    citySearchOption.pageCapacity = 50;
    citySearchOption.city= _currCity;
    citySearchOption.keyword = searchBar.text;
    BOOL flag = [poisearch poiSearchInCity:citySearchOption];
    if(flag)
    {
        NSLog(@"城市内检索发送成功");
    }
    else
    {
        NSLog(@"城市内检索发送失败");
    }
    
    [searchBar resignFirstResponder];
}

#pragma mark 
#pragma mark == Getter/Setters
-(void)setCurrCity:(NSString *)currCity
{
    _currCity = currCity;
}

-(void)setEndPointCompletionHandler:(void (^)(BMKPoiInfo *))endPointCompletionHandler
{
    _endPointCompletionHandler = endPointCompletionHandler;
}

-(void)setStartPointCompletionHandler:(void (^)(BMKPoiInfo *))startPointCompletionHandler
{
    _startPointCompletionHandler = startPointCompletionHandler;
}

@end
