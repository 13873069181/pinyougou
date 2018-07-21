app.controller('searchController', function ($scope, $location, searchService) {
    //定义搜索对象，category：商品分类
    $scope.searchMap = {'keywords':'', 'category':'','brand':'', 'spec':{}, 'price':'', 'pageNo':1, 'pageSize':40, 'sort':'', 'sortField':''}; //搜索对象
    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);  //转换为数字
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            //$scope.searchMap.pageNo = 1;  //每次查询搜索后都显示第一页
            builderPage();  //构建分页栏
        });
    }

    //构建分页栏
    builderPage = function () {
        $scope.pageLabel = [];
        var firstPage = 1;  //开始页码
        var lastPage = $scope.resultMap.totalPages;  //截止页码
        $scope.firstDot = true;  //分页栏中前面的三个点
        $scope.lastDot = true;  //分页栏中后面的三个点

        if ($scope.resultMap.totalPages > 5) {
            //如果总页码数大于5
            if ($scope.searchMap.pageNo <= 3) {
                //如果当前页码小于等于3，显示前5页
                lastPage = 5;
                $scope.firstDot = false;
            } else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages-2) {
                //如果当前页大于等于总页数减2，那么显示最后5页
                firstPage = $scope.resultMap.totalPages - 4;
                $scope.lastDot = false;
            } else {
                //显示以firstPage当前页为中心的5页
                firstPage = $scope.searchMap.pageNo -2;
                lastPage = $scope.searchMap.pageNo +2;
            }

        } else {
            $scope.firstDot = false;  //分页栏中前面的三个点
            $scope.lastDot = false;  //分页栏中后面的三个点
        }

        //构建页码
        for(var i = firstPage; i<= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //添加搜索项，改变searchMap的值
    $scope.addSearchMap = function (key, value) {
        if (key == "category" || key == "brand" || key == 'price') {
            //如果用户点击的是分类或者品牌
            $scope.searchMap[key] = value;
        } else {
            //如果用户点击的是规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();  //查询
    }

    //撤销搜索项
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == 'price') {
            //如果用户点击的是分类或者品牌
            $scope.searchMap[key] = "";
        } else {
            //如果用户点击的是规格
            delete $scope.searchMap.spec[key] ;
        }
        $scope.search();  //查询
    }

    //分页查询
    $scope.queryByPage = function (pageNo) {
        if (pageNo <1 || pageNo > $scope.resultMap.totalPages) {
            return ;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否为最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //排序查询
    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    }

    //判断关键字是否是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }

    //加载关键字
    $scope.loadKeyWords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();  //查询
    }
})