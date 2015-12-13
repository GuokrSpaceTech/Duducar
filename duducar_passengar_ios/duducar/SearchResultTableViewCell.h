//
//  SearchResultTableViewCell.h
//  duducar
//
//  Created by mactop on 12/9/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SearchResultTableViewCell : UITableViewCell

@property (nonatomic,strong) UILabel *addrNameLabel;
@property (nonatomic,strong) UILabel *addrDetailLabel;

-(void)setCellContentAddrName:(NSString *)addrName withAddrDetail:(NSString *)addrDetail;
@end
