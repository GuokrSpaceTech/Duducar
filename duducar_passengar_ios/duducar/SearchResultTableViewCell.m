//
//  SearchResultTableViewCell.m
//  duducar
//
//  Created by mactop on 12/9/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "SearchResultTableViewCell.h"
#import "Masonry.h"

@implementation SearchResultTableViewCell
-(instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    if(self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])
    {
        _addrNameLabel = [[UILabel alloc]init];
        _addrNameLabel.font = [UIFont systemFontOfSize:16];
        _addrNameLabel.backgroundColor = [UIColor clearColor];
        
        _addrDetailLabel = [[UILabel alloc]init];
        _addrDetailLabel.font = [UIFont systemFontOfSize:14];
        _addrDetailLabel.backgroundColor = [UIColor clearColor];
        
        [self.contentView addSubview:_addrNameLabel];
        [self.contentView addSubview:_addrDetailLabel];
    }
    
    return self;
}

-(void)setCellContentAddrName:(NSString *)addrName withAddrDetail:(NSString *)addrDetail
{
    _addrNameLabel.text = addrName;
    _addrDetailLabel.text = addrDetail;

    double addrNameHeight;
    double addrDetailHeight;
    CGRect contentRect = [_addrNameLabel.text
                          boundingRectWithSize:CGSizeMake(220, 0)
                          options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading
                          attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:16.0]}
                          context:nil];
    addrNameHeight = contentRect.size.height;

    contentRect = [_addrDetailLabel.text
                   boundingRectWithSize:CGSizeMake(220, 0)
                   options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading
                   attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:14.0]}
                   context:nil];
    addrDetailHeight = contentRect.size.height;
    
    [_addrNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.contentView.mas_top);
        make.left.equalTo(self.contentView.mas_left).offset(8);
        make.right.equalTo(self.contentView.mas_right).offset(-8);
        make.height.mas_equalTo(addrNameHeight);
    }];
    
    [_addrDetailLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(_addrNameLabel.mas_bottom);
        make.left.equalTo(self.contentView.mas_left).offset(8);
        make.right.equalTo(self.contentView.mas_right).offset(-8);
        make.height.mas_equalTo(addrDetailHeight);
    }];
}

@end
