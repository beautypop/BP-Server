import scrapy
from scrapy.contrib.spiders import CrawlSpider,Rule
from scrapy.contrib.linkextractors.sgml import SgmlLinkExtractor
from scrapy.selector import HtmlXPathSelector
from scrapy.http.request import Request
from scrapy.selector import Selector
from scrapy.http.cookies import CookieJar
import traceback
import csv

class CrawlerSpider(CrawlSpider):
	
	handle_httpstatus_list = [999]
	COOKIES_DEBUG = True
	name = "time"
	allowed_domains = ["time.com"]
	start_urls = [
		'https://hk.search.shop.yahoo.com/search/hkauction/product?cid=23172'
	]
	
	def parse(self, response):
		buyer_file =  '../buyer.csv'
		posts = Selector(text=response.body).xpath("//div[contains(@class, 'srp-pdtitle')]")
		f = open(buyer_file, 'wb')
		wr = csv.writer(f, delimiter=',')

		for post in posts:
			user_name = post.xpath("a/@href").extract()[0].encode('utf-8').split('?u=')[1]
			yield Request("https://hk.user.auctions.yahoo.com/hk/show/rating?userID="+user_name, callback=self.parse_page1, dont_filter=True)
			try:	
				post_name = post.xpath("a/@title").extract()[0].encode('utf-8')
				post_path = post.xpath("a/@href").extract()[0].encode('utf-8')
				data = user_name+","+post_name
				data = data.split(",")
				wr.writerow(data)
			except Exception:
				print 'Unable to parse : '
				print post
				traceback.print_exc()
		return 
	
	def parse_page1(self, response):
		seller_file = '../seller.csv'
		sellers = Selector(text = response.body).xpath("//a[contains(@class,'author')]/@href")
		
		with open(seller_file,'a') as wr:
        
			for seller in sellers:
				try:	
					data = []
					user_name = seller.extract().encode('utf-8').split('author=')[1]
					data.append(user_name)
					wr.write(user_name+"\n")
				except IndexError as inst:
					print 'Unable to parse seller : '
					print seller.extract()
			
		
		return 
		

