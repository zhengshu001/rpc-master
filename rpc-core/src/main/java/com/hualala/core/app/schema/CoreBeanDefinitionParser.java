package com.hualala.core.app.schema;

import com.hualala.core.app.bean.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


public class CoreBeanDefinitionParser implements BeanDefinitionParser {

    private Class<?> className;
    public CoreBeanDefinitionParser(Class<?> className) {
        this.className = className;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        if (className.equals(FilterBean.class)) {
            RootBeanDefinition filterDef = new RootBeanDefinition();
            filterDef.setBeanClass(this.className);
            filterDef.setLazyInit(false);
            String id = element.getAttribute(FilterBean.ATTR_ID);
            if (element.hasAttribute(FilterBean.ATTR_SERVICE)) {
                filterDef.getPropertyValues().addPropertyValue(FilterBean.ATTR_SERVICE, element.getAttribute(FilterBean.ATTR_SERVICE));
            }
            if (element.hasAttribute(FilterBean.ATTR_METHOD)) {
                filterDef.getPropertyValues().addPropertyValue(FilterBean.ATTR_METHOD, element.getAttribute(FilterBean.ATTR_METHOD));
            }
            parserContext.getRegistry().registerBeanDefinition(id, filterDef);
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String localName = node.getLocalName();
                if ((FilterBean.ELE_REQUEST.equals(localName) && !element.hasAttribute(FilterBean.ELE_REQUEST))) {
                    RequestBean requestBean = doRequestParse((Element)node);
                    filterDef.getPropertyValues().addPropertyValue(FilterBean.ELE_REQUEST, requestBean);
                } else if ((FilterBean.ELE_RESPONSE.equals(localName) && !element.hasAttribute(FilterBean.ELE_RESPONSE))) {
                    ResponseBean responseBean = doResponseParse((Element)node);
                    filterDef.getPropertyValues().addPropertyValue(FilterBean.ELE_RESPONSE, responseBean);
                }
            }
            return filterDef;
        }
        return null;
    }

	protected Class getBeanClass(Element element) {  
        return FilterBean.class;  
    } 

    private RequestBean doRequestParse(Element requestEle) {
        RequestBean requestBean = new RequestBean();
        List<DataBean> dataBeanList = doDataParse(requestEle);
        requestBean.setDataBeens(dataBeanList);
        if (requestEle.hasAttribute(RequestBean.ATTR_INCLUDE)) {
            requestBean.setInclude(Boolean.valueOf(requestEle.getAttribute(RequestBean.ATTR_INCLUDE)));
        }
        return requestBean;
    }

    private ResponseBean doResponseParse(Element responseEle) {
        ResponseBean responseBean = new ResponseBean();
        List<DataBean> dataBeanList = doDataParse(responseEle);
        responseBean.setDataBeens(dataBeanList);
        if (responseEle.hasAttribute(ResponseBean.ATTR_INCLUDE)) {
            responseBean.setInclude(Boolean.valueOf(responseEle.getAttribute(RequestBean.ATTR_INCLUDE)));
        }
        return responseBean;
    }

    private ListBean doListParse(Element listEle) {
        return new ListBean();
    }

    private MapBean doMapParse(Element mapBean) {
        return new MapBean();
    }

    private List<DataBean> doDataParse(Element listEle) {
        List<DataBean> retList = new ArrayList<>();
        NodeList dataNodeList = listEle.getChildNodes();
        for (int i = 0; i < dataNodeList.getLength(); i++) {
            Node node = dataNodeList.item(i);
            if (DataBean.ELE_NAME.equals(node.getLocalName())) {
                Element data = ((Element)node);
                String key = data.getAttribute(DataBean.ATTR_KEY);
                DataBean dataBean = new DataBean(key);
                if (data.hasAttribute(DataBean.ATTR_VALUE)) {
                    dataBean.setValue(data.getAttribute(DataBean.ATTR_VALUE));
                } else if (data.hasAttribute(DataBean.ATTR_SOURCE)) {
                    dataBean.setSource(data.getAttribute(DataBean.ATTR_SOURCE));
                } else if (data.hasAttribute(DataBean.ATTR_EXPRESSION)) {
                    dataBean.setExpression(data.getAttribute(DataBean.ATTR_EXPRESSION));
                }
                if (data.hasAttribute(DataBean.ATTR_TYPE)) {
                    dataBean.setType(DataBean.Type.valueOf(data.getAttribute(DataBean.ATTR_TYPE).toUpperCase()));
                    if (dataBean.getType() == DataBean.Type.MAP) {
                        MapBean mapBean = doMapParse(data);
                    } else if (dataBean.getType() == DataBean.Type.LIST) {
                        ListBean listBean = doListParse(data);
                    }
                }
                if (data.hasAttribute(DataBean.ATTR_EXTRA)) {
                    dataBean.setExtra(data.getAttribute(DataBean.ATTR_EXTRA));
                }
                if (data.hasAttribute(DataBean.ATTR_DELETE)) {
                    dataBean.setDelete(Boolean.valueOf(data.getAttribute(DataBean.ATTR_DELETE)));
                }
                NodeList dictNodeList = node.getChildNodes();
                List<DictBean> dictBeenList = new ArrayList<>();
                for (int j = 0; j < dictNodeList.getLength(); j++) {
                    Node dictNode = dictNodeList.item(j);
                    if (DictBean.ELE_NAME.equals(dictNode.getLocalName())) {
                        Element dictData = ((Element)dictNode);
                        dictBeenList.add(new DictBean(dictData.getAttribute(DictBean.ATTR_KEY), dictData.getAttribute(DictBean.ATTR_VALUE)));
                    }
                }
                if (dictBeenList.size() > 0) {
                    dataBean.setDictBeens(dictBeenList);
                }
                retList.add(dataBean);
            }
        }
        return retList;
    }


}
