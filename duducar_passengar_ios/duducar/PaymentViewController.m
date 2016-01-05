//
//  PaymentViewController.m
//  duducar
//
//  Created by mactop on 12/11/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "PaymentViewController.h"
#import "RatingViewController.h"
#import <AlipaySDK/AlipaySDK.h>
#import "WXApiRequestHandler.h"
#import "DDDatabase.h"
#import "Order.h"
#import "DataSigner.h"

//Alipay Secrets
static NSString *partner = @"2088121002293318";
static NSString *seller = @"1946742250@qq.com";
static NSString *privateKey =
@"MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOpsoA3pTu1jxVg4"
"2GVX+niS2Y0UQHe4uxn5lIDiOOvOPhBDFrPhVi8s4RTWR89PGQSOLlH6CzyUXNwV"
"98aFrditHLNk+zuOetm/gRU7dzA9SsZTGQ2e2oT39EWft07R4WoZmXRP7B3Xp8U8"
"6U8rO578M8w4ZA1KHMwkxxjqhPJ9AgMBAAECgYBADnad1obOr1iZhs76wlOa5uWz"
"ezkyfbQCoQRHQ4myRaUH5I0rkgNu2KCYhQUSTNbVO9TEacLwRsopCYevI5AhAxxT"
"ANGoL4eeYSeaYZJoBiUeYu6UpX78Hhy/GWNVDFLkm42FT9Il3Zi0bf/jtg/mVmzK"
"k8NzA0ePf994ALvOMQJBAPxOKrViXOh64s3n3W3cZ3F+dXLsBWhnNOzYlT5cSGoQ"
"K4ud0bGGIDs7LQ72poJwARXd4H0ZwJR4rwMnoQbIFF8CQQDt2204/ndcwOx38iQs"
"V7AkpCrK1WEg11tK2lBJE3TiaiIoZhgbWNCc9ZJO79UeTuYku5MXx8XHuw+WZs23"
"MkajAkEAsknuRiSO8L09njE1uNdhxcKN7jq4i5E6xg86T0nY5hItI0jPkDnudsyX"
"R5amDVBmg/Q5GU3kV0Z8racIVAl40wJAfsm2YOkTyzdzVUSXj6N2WzG/NbukOJNT"
"MIVKwolChuY4Kvyw4PLo0KH+SWGCYtN/zhjGgaiVfq/x0SQfiAWerQJBAIVWunH9"
"KckyXpEIFhCeIbx5blSZ2OTcDzqm++GsjP9eFxDxluqSolglnaQpJEwg6PeoWhiw"
"kYSGL5z3CmCjQCI=";

enum Paymethod{
    ALIPAY,
    WECHATPAY
};

@interface PaymentViewController ()
{
    enum Paymethod paymenthod;
}
@end

@implementation PaymentViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    /*
     * Init UI
     */
    //导航条
    UIBarButtonItem * leftItem = [[UIBarButtonItem alloc]initWithTitle:@"取消支付" style:UIBarButtonItemStyleDone target:self action:@selector(back:)];
    self.navigationItem.leftBarButtonItem = leftItem;
    self.navigationItem.title = @"支付";
    
    if ([self respondsToSelector:@selector(edgesForExtendedLayout)])
        self.edgesForExtendedLayout = UIRectEdgeNone;
    
    if(_chargePrice)
    {
        self.chargeLabel.text = _chargePrice;
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(payResult:) name:@"WechatPayResultNotification" object:nil];
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void)setActiveOrder:(NSDictionary *)activeOrder
{
    _activeOrder = activeOrder;
    if(_activeOrder)
    {
        NSNumber *sumprice = [_activeOrder objectForKey:@"sumprice"];
        _chargePrice = [NSString stringWithFormat:@"%@", sumprice];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark
#pragma mark == User Action
-(void)back:(id)sender
{
    //确认取消订单
    
    UIAlertView * alert = [[UIAlertView alloc]initWithTitle:@"提示" message:@"是否取消支付" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
    [alert show];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex ==0 )
    {
        NSLog(@"取消");
    }
    else if(buttonIndex == 1)
    {
        //确定, Back2MainView
        [self.navigationController popToRootViewControllerAnimated:YES];
    }
}

- (IBAction)paymentAction:(id)sender {
    if(paymenthod == ALIPAY)
    {
        [self alipay];
    } else if(paymenthod == WECHATPAY)
    {
        [self wechatpay];
    }
}

- (IBAction)aliPaySelected:(id)sender {
    [_alipayButton setImage:[UIImage imageNamed:@"checked"] forState:UIControlStateNormal];
    [_weichatpayButton setImage:[UIImage imageNamed:@"unchecked"] forState:UIControlStateNormal];
    paymenthod = ALIPAY;
}

- (IBAction)WechatPaySelected:(id)sender {
    paymenthod = WECHATPAY;
    [_alipayButton setImage:[UIImage imageNamed:@"unchecked"] forState:UIControlStateNormal];
    [_weichatpayButton setImage:[UIImage imageNamed:@"checked"] forState:UIControlStateNormal];
}

-(void) alipay
{
    Order *order = [[Order alloc] init];
    order.partner = partner;
    order.seller = seller;
    order.tradeNO = [self generateTradeNO]; //订单ID（由商家自行制定）
    order.productName = @"嘟嘟专车"; //商品标题
    order.productDescription = @"支付车费"; //商品描述
    order.amount = _chargePrice; //商品价格
    order.notifyURL = @"http://120.24.237.15:81/api/Pay/getAlipayResult"; //回调URL
    order.service = @"mobile.securitypay.pay";
    order.paymentType = @"1";
    order.inputCharset = @"utf-8";
    order.itBPay = @"30m";
    
    //应用注册scheme,在AlixPayDemo-Info.plist定义URL types
    NSString *appScheme = @"duducar";
    
    //将商品信息拼接成字符串
    NSString *orderSpec = [order description];
    NSLog(@"orderSpec = %@",orderSpec);
    
    //获取私钥并将商户信息签名,外部商户可以根据情况存放私钥和签名,只需要遵循RSA签名规范,并将签名字符串base64编码和UrlEncode
    id<DataSigner> signer = CreateRSADataSigner(privateKey);
    NSString *signedString = [signer signString:orderSpec];
    
    //将签名成功字符串格式化为订单字符串,请严格按照该格式
    NSString *orderString = nil;
    if (signedString != nil) {
        orderString = [NSString stringWithFormat:@"%@&sign=\"%@\"&sign_type=\"%@\"",
                       orderSpec, signedString, @"RSA"];
        
        [[AlipaySDK defaultService] payOrder:orderString fromScheme:appScheme callback:^(NSDictionary *resultDic) {
            //【callback处理支付结果】
            NSLog(@"reslut = %@",resultDic);
            int retCode = (int)[resultDic objectForKey:@"resultStatus"];
            NSString *retMsg = [resultDic objectForKey:@"result"];
            //支付成功
            if(retCode == 9000)
            {
                RatingViewController *rateVC = [[RatingViewController alloc] initWithNibName:@"RatingViewController" bundle:nil];
                rateVC.activeOrder = _activeOrder;
                rateVC.driver = _driver;
                [self.navigationController pushViewController:rateVC animated:YES];
            } else {
                UIAlertView *alter = [[UIAlertView alloc] initWithTitle:@"支付未完成" message:retMsg delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alter show];
            }
        }];
    }
}

-(void)wechatpay
{
    __block NSString *mobile;
    __block NSString *usertoken;
    id orderNum = [_activeOrder objectForKey:@"orderNum"];
    NSString *startStr = [_activeOrder objectForKey:@"start"];
    NSString *destStr  = [_activeOrder objectForKey:@"destination"];
    NSString *body = [NSString stringWithFormat:@"%@-%@", startStr, destStr];
    NSString *price = _chargePrice;
    
    [[DDDatabase sharedDatabase]selectFromPersonInfo:^(NSString *token, NSString *phone) {
        mobile = phone;
        usertoken = token;
    }];
    
    NSDictionary *paramDict = @{@"orderNum":orderNum, @"body":body, @"total_fee":price, @"token":usertoken, @"role":@"2", @"mobile":mobile };

    NSString *res = [WXApiRequestHandler jumpToBizPay:paramDict];
    if( ![@"" isEqual:res] ){
        UIAlertView *alter = [[UIAlertView alloc] initWithTitle:@"支付失败" message:res delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alter show];
    }
}

#pragma mark -
#pragma mark   ==============产生随机订单号==============
- (NSString *)generateTradeNO
{
    static int kNumber = 15;
    
    NSString *sourceStr = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    NSMutableString *resultStr = [[NSMutableString alloc] init];
    srand(time(0));
    for (int i = 0; i < kNumber; i++)
    {
        unsigned index = rand() % [sourceStr length];
        NSString *oneStr = [sourceStr substringWithRange:NSMakeRange(index, 1)];
        [resultStr appendString:oneStr];
    }
    return resultStr;
}

#pragma mark -
#pragma mark   ==============通知处理==============
-(void)payResult:(NSNotification *)notification
{
    NSDictionary *result = notification.userInfo;
    if(result)
    {
        if([[result objectForKey:@"errCode"] intValue]  == WXSuccess)
        {
            RatingViewController *rateVC = [[RatingViewController alloc] initWithNibName:@"RatingViewController" bundle:nil];
            rateVC.activeOrder = _activeOrder;
            rateVC.driver = _driver;
            [self.navigationController pushViewController:rateVC animated:YES];
        } else {
            //Do nothing
        }
    }
}
@end
