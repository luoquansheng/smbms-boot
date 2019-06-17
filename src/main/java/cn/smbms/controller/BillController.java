package cn.smbms.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.BillService;
import cn.smbms.service.ProviderService;
import cn.smbms.service.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/sys/bill")
public class BillController extends BaseController {
	private Logger logger = Logger.getLogger(BillController.class);

	@Resource
	private BillService billService;
	@Resource
	private ProviderService providerService;
	@Resource
	private UserService userService;

	@RequestMapping(value = "/list.html")
	public String getBillList(Model model,
			@RequestParam(value = "queryProductName", required = false) String queryProductName,
			@RequestParam(value = "queryProviderId", required = false) String queryProviderId,
			@RequestParam(value = "queryIsPayment", required = false) String queryIsPayment,
			@RequestParam(value = "pageIndex", required = false) String pageIndex) {
		logger.info("getBillList ---- > queryProductName: " + queryProductName);
		logger.info("getUserList ---- > queryProviderId: " + queryProviderId);
		logger.info("getUserList ---- > queryIsPayment: " + queryIsPayment);
		logger.info("getUserList ---- > pageIndex: " + pageIndex);
		Integer _queryProviderId = null;
		Integer _queryIsPayment = null;
		List<Bill> billList = null;
		List<Provider> providerList = null;
		// 设置页面容量
		int pageSize = Constants.pageSize;
		// 当前页码
		int currentPageNo = 1;

		if (queryProductName == null) {
			queryProductName = "";
		}
		if (queryProviderId != null && !queryProviderId.equals("")) {
			_queryProviderId = Integer.parseInt(queryProviderId);
		}
		if (queryIsPayment != null && !queryIsPayment.equals("")) {
			_queryIsPayment = Integer.parseInt(queryIsPayment);
		}

		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (NumberFormatException e) {
				return "redirect:/syserror.html";
			}
		}
		// 总数量（表）
		int totalCount = 0;
		try {
			totalCount = billService.getBillCount(queryProductName, _queryProviderId, _queryIsPayment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		// 控制首页和尾页
		if (currentPageNo < 1) {
			currentPageNo = 1;
		} else if (currentPageNo > totalPageCount) {
			currentPageNo = totalPageCount;
		}
		try {
			billList = billService.getBillList(queryProductName, _queryProviderId, _queryIsPayment, currentPageNo,
					pageSize);
			providerList = providerService.getProviderList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("billList", billList);
		model.addAttribute("providerList", providerList);
		model.addAttribute("queryProductName", queryProductName);
		model.addAttribute("queryProviderId", queryProviderId);
		model.addAttribute("queryIsPayment", queryIsPayment);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		model.addAttribute("contentPage", "billlist");
		return "main";
	}

	@RequestMapping(value = "/add.html", method = RequestMethod.GET)
	public String addBill(@ModelAttribute("bill") Bill bill, Model model) {
		model.addAttribute("contentPage", "billadd");
		return "main";
	}

	@RequestMapping(value = "/addsave.html", method = RequestMethod.POST)
	public String addBillSave(Bill bill, HttpSession session, Model model) {
		bill.setModifyBy(((User) userService.getUserByCode(session.getAttribute(Constants.USER_SESSION).toString())).getId());
		bill.setCreationDate(new Date());
		try {
			if (billService.add(bill)) {
				return "redirect:/sys/bill/list.html";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("contentPage", "billadd");
		return "main";
	}

	@RequestMapping(value = "/modify/{id}", method = RequestMethod.GET)
	public String getBillById(@PathVariable String id, Model model, HttpServletRequest request) {
		Bill bill = new Bill();
		try {
			bill = billService.getBillById(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute(bill);
		model.addAttribute("contentPage", "billmodify");
		return "main";
	}

	@RequestMapping(value = "/modifysave.html", method = RequestMethod.POST)
	public String modifyBillSave(Bill bill, HttpSession session, Model model) {
		bill.setModifyBy(((User) userService.getUserByCode(session.getAttribute(Constants.USER_SESSION).toString())).getId());
		bill.setModifyDate(new Date());
		try {
			if (billService.modify(bill)) {
				//model.addAttribute("contentPage", "billlist");
				return "redirect:/sys/bill/list.html";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("contentPage", "billmodify");
		return "main";
	}

	@RequestMapping(value = "/providerlist.json", method = RequestMethod.GET, produces = {
			"application/json;charset=UTF-8" })
	@ResponseBody
	public List<Provider> getProviderList() {
		List<Provider> providerList = null;
		try {
			providerList = providerService.getProviderList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("providerList size: " + providerList.size());
		return providerList;
	}

	@RequestMapping(value = "/delbill.json", method = RequestMethod.GET)
	@ResponseBody
	public Object delBill(@RequestParam String id) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if (StringUtils.isNullOrEmpty(id)) {
			resultMap.put("delResult", "notexist");
		} else {
			try {
				if (billService.deleteBillById(Integer.parseInt(id)))
					resultMap.put("delResult", "true");
				else
					resultMap.put("delResult", "false");
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JSONArray.toJSONString(resultMap);
	}

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String view(@PathVariable String id, Model model, HttpServletRequest request) {
		logger.debug("view id===================== " + id);
		Bill bill = new Bill();
		try {
			bill = billService.getBillById(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute(bill);
		model.addAttribute("contentPage", "billview");
		return "main";
	}

}
