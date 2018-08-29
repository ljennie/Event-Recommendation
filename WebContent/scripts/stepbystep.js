////(主函数，所有js代码)(运行代码)
//(function() {
//
//	/* step2: variables */
//	//	模拟用户名和地址
//	var user_id = '1111';
//	var user_fullname = 'John Smith';
//	var lng = -122.08;
//	var lat = 37.38;
//
//	/* step3: main function(entrance) */
//	init();//入口函数，工业界应用目的是为了让代码更模块化，更清楚
//
//	/* step4: define init function */
//	function init() {
//		// Register event listeners //点击Nearby会跳出相应的东西
//		$('nearby-btn').addEventListener('click', loadNearbyItems);//'click'以后要出现loadNearbyItems)
//		// $('fav-btn').addEventListener('click', loadFavoriteItems);
//		// $('recommend-btn').addEventListener('click', loadRecommendedItems);
//
//		var welcomeMsg = $('welcome-msg');//右上角欢迎这个标记//$ j query,自定义模拟的函数
//		welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;
//
//		// step 7
//		initGeoLocation();
//	}
//
//	/* step5: create $ function */
//	/**
//	 * A helper function that creates a DOM element <tag options...>
//	 */
//	function $(tag, options) {//$ 函数名
//		if (!options) {
//			return document.getElementById(tag);
//		}
//		var element = document.createElement(tag);
//
//		for ( var option in options) {
//			if (options.hasOwnProperty(option)) {
//				element[option] = options[option];
//			}
//		}
//		return element;
//	}
//
//	/* step6: create AJAX helper function */
//	/**
//	 * @param method -
//	 *            GET|POST|PUT|DELETE
//	 * @param url -
//	 *            API end point
//	 * @param callback -
//	 *            This the successful callback
//	 * @param errorHandler -
//	 *            This is the failed callback
//	 */
//	function ajax(method, url, data, callback, errorHandler) {
//		var xhr = new XMLHttpRequest();//现有一个object
//
//		xhr.open(method, url, true);//告知使用的方法
//				// get/post
//
//		xhr.onload = function() {//onload是ajax的一个自带的方法
//			//当我进入的时候要查看他的状态
//			if (xhr.status === 200) {
//				callback(xhr.responseText);//callback!!! 客户端发送给server，server发过来的status
//				//callback： 函数作为参数传递给另一个函数，当需要的时候
//				//例如：我在商场买衣服，但我喜欢的那个颜色没有了，于是我留了个电话号码给店员。当这个号色的衣服有的时候，
//				//店员就会打电话给我通知我去拿。这个过程中电话号码就是callback
//				// callback可以很复杂，callback可以实现异步同行。
//			    // 服务器可以处理接受各种request，然后一一返回response，但并不影响整个程序的执行
//			} else if (xhr.status === 403) {
//				onSessionInvalid();
//			} else {
//				errorHandler();
//			}
//		};
//
//		xhr.onerror = function() {//xhr有error的时候
//			console.error("The request couldn't be completed.");
//			errorHandler();
//		};
//
//		if (data === null) {
//			xhr.send();
//		} else {//有data的时候
//			xhr.setRequestHeader("Content-Type",
//					"application/json;charset=utf-8");
//			//传送json的数据类型，然后进行数据的传递
//			xhr.send(data);
//		}
//	}
//
//	/** step 7: initGeoLocation function * */
//	function initGeoLocation() {//一点进来以后不需要点击就可以出现自己附近的信息
//		if (navigator.geolocation) {//自带的geolocation,看是否存在
//			// step 8
//			navigator.geolocation.getCurrentPosition(onPositionUpdated,//存在的话，自动更新，位置的更新
//					onLoadPositionFailed, {
//						maximumAge : 60000
//					});
//			showLoadingMessage('Retrieving your location...');
//		} else {
//			// step 9
//			onLoadPositionFailed();
//		}
//	}
//
//	/** step 8: onPositionUpdated function * */
//	function onPositionUpdated(position) {
//		lat = position.coords.latitude;
//		lng = position.coords.longitude;
//
//		// step 11
//		loadNearbyItems();
//	}
//
//	/** step 9: onPositionUpdated function * */
//	function onLoadPositionFailed() {
//		console.warn('navigator.geolocation is not available');
//
//		// step 10
//		getLocationFromIP();
//	}
//
//	/** step 10: getLocationFromIP function * */
//	function getLocationFromIP() {
//		// Get location from http://ipinfo.io/json
//		var url = 'http://ipinfo.io/json'
//		var req = null;
//		ajax('GET', url, req, function(res) {
//			var result = JSON.parse(res);
//			if ('loc' in result) {
//				var loc = result.loc.split(',');
//				lat = loc[0];
//				lng = loc[1];
//			} else {
//				console.warn('Getting location by IP failed.');
//			}
//			// step 11
//			loadNearbyItems();
//		});
//	}
//
//	/** step 11: loadNearbyItems function * */
//	/**
//	 * API #1 Load the nearby items API end point: [GET]
//	 * /Dashi/search?user_id=1111&lat=37.38&lon=-122.08
//	 */
//	function loadNearbyItems() {
//		console.log('loadNearbyItems');
//		// step 12
//		activeBtn('nearby-btn');//激活这个功能
//
//		// The request parameters
//		var url = './search';
//		var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
//		var req = JSON.stringify({});//js转化为等价json字符
//
//		// step 13
//		// display loading message
//		showLoadingMessage('Loading nearby items...');
//
//		// make AJAX call
//		ajax('GET', url + '?' + params, req,
//		// successful callback
//		function(res) {
//			var items = JSON.parse(res);
//			if (!items || items.length === 0) {
//				// step 14
//				showWarningMessage('No nearby item.');
//			} else {
//				// step 16
//				listItems(items);
//			}
//		},
//		// failed callback
//		function() {
//			// step 15
//			showErrorMessage('Cannot load nearby items.');
//		});
//	}
//	/** step 12: activeBtn function * */
//
//	/**
//	 * A helper function that makes a navigation button active
//	 * 
//	 * @param btnId -
//	 *            The id of the navigation button
//	 */
//	function activeBtn(btnId) {
//		var btns = document.getElementsByClassName('main-nav-btn');
//
//		// deactivate all navigation buttons
//		for (var i = 0; i < btns.length; i++) {//把没有选中的button给取消掉
//			btns[i].className = btns[i].className.replace(/\bactive\b/, '');
//		}												// regular expression, 就是找到active的标志然后替换到空格
//
//		// active the one that has id = btnId
//		var btn = $(btnId);
//		btn.className += ' active'; //对激活的button增加active的类
//	}
//
//	/** step 13: showLoadingMessage function * */
//	function showLoadingMessage(msg) {
//		var itemList = $('item-list');
//		itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> '
//				+ msg + '</p>';
//	}// step 14，15 warningMessage和errorMessage
//
//	/** step 14: showWarningMessage function * */
//	function showWarningMessage(msg) {
//		var itemList = $('item-list');
//		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> '
//				+ msg + '</p>';
//	}
//
//	/** step15: showErrorMessage function * */
//	function showErrorMessage(msg) {
//		var itemList = $('item-list');
//		itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> '
//				+ msg + '</p>';
//	}
//
//	/** step16: listItems function * */
//	/**
//	 * @param items -
//	 *            An array of item JSON objects
//	 */
//	function listItems(items) {//items就是json格式的所有item信息
//		// Clear the current results
//		var itemList = $('item-list');
//		itemList.innerHTML = '';
//
//		for (var i = 0; i < items.length; i++) {
//			// step 17
//			addItem(itemList, items[i]);//addItem实现一条条数据的添加
//				 // 添加的数据   添加的地址
//		}
//	}
//
//	/** step17: addItem function * */
//	/**
//	 * Add item to the list
//	 * 
//	 * @param itemList -
//	 *            The
//	 *            <ul id="item-list">
//	 *            tag
//	 * @param item -
//	 *            The item data (JSON object)
//	 */
//	function addItem(itemList, item) {
//		var item_id = item.item_id;
//
//		// create the <li> tag and specify the id and class attributes
//		var li = $('li', {
//			id : 'item-' + item_id,
//			className : 'item'
//		});
//
//		// set the data attribute
//		li.dataset.item_id = item_id;
//		li.dataset.favorite = item.favorite;
//
//		// item image
//		if (item.image_url) {
//			li.appendChild($('img', {//看有没有图片，有的话添加
//				src : item.image_url
//			}));
//		} else {
//			li.appendChild($('img',{//没图片的话添加默认图片
//				src : 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
//			}))
//		}
//		// section
//		var section = $('div', {});
//
//		// title
//		var title = $('a', {
//			href : item.url,
//			target : '_blank',
//			className : 'item-name'
//		});
//		title.innerHTML = item.name;
//		section.appendChild(title);
//
//		// category
//		var category = $('p', {
//			className : 'item-category'
//		});
//		category.innerHTML = 'Category: ' + item.categories.join(', ');
//		section.appendChild(category);
//
//		var stars = $('div', {
//			className : 'stars'
//		});
//
//		for (var i = 0; i < item.rating; i++) {
//			var star = $('i', {
//				className : 'fa fa-star'
//			});
//			stars.appendChild(star);
//		}
//
//		if (('' + item.rating).match(/\.5$/)) {
//			stars.appendChild($('i', {
//				className : 'fa fa-star-half-o'
//			}));
//		}
//
//		section.appendChild(stars);
//
//		li.appendChild(section);
//
//		// address
//		var address = $('p', {
//			className : 'item-address'
//		});
//
//		address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g,
//				'');
//		li.appendChild(address);
//
//		// favorite link
//		var favLink = $('p', {
//			className : 'fav-link'
//		});
//
//		favLink.onclick = function() {
//			changeFavoriteItem(item_id);
//		};
//
//		favLink.appendChild($('i', {
//			id : 'fav-icon-' + item_id,
//			className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
//		}));
//
//		li.appendChild(favLink);
//
//		itemList.appendChild(li);
//	}
//
//})()
