# 针对图书进行建模分析

## 首先，图书自有的，在出版后就不再改变的属性。例如书名，作者，出版社，原始定价，ISBN号码，描述图片，等等。
## 其次，图书作为商品，有和商业活动相关的属性。例如价格，折扣，促销组别，库存数目，等等。
## 第三，为了支持当前系统的查找等功能，图书还有一些附加在属性。例如系统ID，关键字，所属分类，等等。

### 图书自有属性
- 书名，可能是中文，也可能是英文
- 作者
- 原始定价
- 所属分类
- 出版时间
- ISBN(不知道有什么业务作用，留着吧)
- 描述图片的地址
- 出版社(业务作用不详，留着吧)
- 字符次序(用于简化构建字符字典的算法。例如，'三国志'的字符次序就定为'sanguozhi'，不同图书的字符顺序可能雷同，因此在设计搜索的时候要考虑这种情形)

### 图书商业属性
- 价格
- 针对此商品的折扣比率。例如某商品定价100，折扣比率70%，则折扣后价格=100 * 70% =70
- 促销组别。某个商品可能同时属于多个促销组，每个促销组有其自己的计算方法。
- 库存数目

### 系统内部属性
- 商品唯一ID(商品编码)
- 关键字，或者说tag。某个商品可能有多个tag

### 促销组：
例如'满300-50'，'满3件免最贵的一件'，等等。辅助角色，用于在生成订单时对订单价格进行核算，用于丰富系统的促销场景。可以作为远期的扩展功能。

问题：系统中是否允许两本书出现冗余？
> 回答：允许。假设某本书的某个版次，由同一个出版社印刷了两次，并且原始定价刚好不同。这就可能出现，同样一本书，系统定价却不同的情形。

问题：如何处理这种情况，或者说，如何定价？
> 回答：首先，在制定采购计划的时候，应当进行搜索，避免重复采购同一本书。
  其次，如果确实因图书更新而需要采购同一本书，并且采购价格和原来不同，那么我们应当尊重这个事实。
  相关图书的定价依旧遵循系统既定策略，不做特殊处理，由消费者自行选择。


## 借由图书模型分析，引申出的针对商品的模型

### 每个商品有其自有的属性
#### 商品的核心属性
例如，商品的名称，类别，等必须具备的属性<br/> 如果缺乏必备的属性，我们就无法对商品进行基本的区分，无法回答'这到底是什么？'<br/>
某种商品可能有不同的型号，例如手机和电脑，不同的型号会有不同的价格。每种配置/型号都对应一种商品，ID各不相同。但是，它们在逻辑上又同属于某'一种'商品。是否可以借助这个特性来实现不同的交易模式(销售，租赁，回收等)？

#### 商品的描述性属性
商品的描述性属性指用于辅助描述商品的属性。商品的描述性属性不是必须的，极端情况下，可能某个商品的'详细参数'页面就是空白。<br/>
不同的商品，可能具有完全不同的描述性属性。但是，我们从'自有的'这个角度来观察，就在不同之中抽象出了相同。<br/>
是否能够采用统一的方法来对商品的自有属性进行建模，操作呢？ <br/>
例如商品A有(书名，作者，出版社)等属性，商品B有(屏幕尺寸，品牌，CPU型号)等属性。那么我们忽略这些属性的文字描述，将它们抽象成(属性1，属性2，属性3)，并允许动态添加属性。那么，商品的属性就被抽象成了一个集合，集合中的每个元素都是一个属性对象(属性名，属性值)。<br/>
为了便于管理，防止随意进行属性的定义(例如管理员a为某手机定义了'屏幕尺寸'这个属性，管理员b为另外一个手机定义了'屏幕大小'这个属性，就出现了不必要的冗余)，我们可以事先定义一些属性，管理员在需要的时候在属性列表中去选择就可以了。<br/>
例如，手机一般都具有屏幕大小，品牌，分辨率等属性，我们就可以建立好相应的属性，并定义好各个属性的默认值。录入员在录入一款新型号的时候，只需要在手机属性类目下，选择相应的属性，并选择相应的值就可以了。如果没有相应的值，可以向管理员申请添加。这样用选择代替了输入，降低了出错率。同时，这里也体现了角色职责的划分。<br/>

### 每个商品的商业属性
在一个商业系统内，每种商品会有其为一ID来标识。不同的厂商可能生产相同名称的商品，同一种商品可能在不同的生产环境下产生不同的定价，同一个商品可能被不同的主体来销售。在这种情况下，此商品非彼商品，我们应当允许'不同的同一个商品'的存在。

### 由此引出的流程定义
- 定义商品类别。一级分类，二级分类等。这种分类应当是相对严谨的那种，和tag相对应。一个商品应当确定属于某个具体的类别，就像一种生物属于某特定的分支。但是一种商品可能带有不同的tag描述，就像一种生物既可以萌，也可以彪悍，还可以美味...
- 定义属性。属性名称，属性值的集合，属性默认值，属性用于描述哪中类别的商品。如果某个属性用于描述某个一级分类，那么这个属性这种分类下的所有二级、三级分类都应当具备这个属性。这类似与继承的模式。
- 添加某个商品，指明商品属于哪个类目，并指定相应的属性。
