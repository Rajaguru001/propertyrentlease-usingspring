package com.chainsys.propertyrentlease.dao;

import java.io.InputStream;

import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.chainsys.propertyrentlease.mapper.SellerPropertyFormMapper;
import com.chainsys.propertyrentlease.mapper.UserMapper;
import com.chainsys.propertyrentlease.model.Comment;
import com.chainsys.propertyrentlease.model.PropertyImage;
import com.chainsys.propertyrentlease.model.SellerDashBoard;
import com.chainsys.propertyrentlease.model.SellerDashBoardRequest;
import com.chainsys.propertyrentlease.model.SellerPropertyForm;
import com.chainsys.propertyrentlease.model.Users;
import com.chainsys.propertyrentlease.mapper.PropertyImageMapper;
import com.chainsys.propertyrentlease.mapper.CommentMapper;
import com.chainsys.propertyrentlease.mapper.SellerDashBoardMapper;
import com.chainsys.propertyrentlease.mapper.SellerDashBoardRequestMapper;

@Repository("propertyRentLeaseImpl")
public class PropertyRentLeaseImpl implements PropertyRentLeaseDAO {
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public PropertyRentLeaseImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean insert(Users users) {
		String query = "SELECT email,password FROM users WHERE email=? and password=?" ;
		String insert = "INSERT INTO users (user_name, password, email, phonenumber) VALUES (?, ?, ?, ?)";

		boolean emailExists = jdbcTemplate.query(query, new Object[] { users.getEmail(),users.getPassword() }, (resultSet) -> {
			return resultSet.next();
		});
		if (!emailExists) {

			jdbcTemplate.update(insert, users.getUsername(), users.getPassword(), users.getEmail(),
					users.getPhonenumber());
			return true;
		}

		return false;
	}

	@Override
	public Users getUserIdByEmail(Users users) {
		try {
			String query = "SELECT user_id, user_name, password, email, phonenumber FROM users WHERE email=?";
			Users user = jdbcTemplate.queryForObject(query, new UserMapper(), users.getEmail());
			return user;
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}
	
	public boolean insertLogin(Users user) {

        String register="SELECT count(*)FROM users WHERE email=? && password=?";
        Object[]email= {user.getEmail(),user.getPassword()};
        int count= jdbcTemplate.queryForObject(register,Integer.class,email);
        if(count==0)
        {
        
        return false;
        }
            
        return true;
        
    }

	@Override
	public Users adminlogincheck(Users users) {
		String query = "select user_id, user_name, password, email, phonenumber from users where email=? and password=?";
		Users user = jdbcTemplate.queryForObject(query, new UserMapper(),
				new Object[] { users.getEmail(), users.getPassword() });
		if (user == null) {
			return null;
		} else {
			return user;
		}

	}

	public SellerPropertyForm loggerInUser(Users user) {
		String query = "select * from property_details WHERE owner_id=?";
		SellerPropertyForm users = jdbcTemplate.queryForObject(query, new SellerPropertyFormMapper(),
				new Object[] { user.getUserid() });
		if (users == null) {
			return null;
		} else {
			return users;
		}
	}

	@Override
	public void savesProperty(int userId, String propertyType, int sqft, String furnishing, int rentPrice,
			String address, String location, java.util.Date availableFrom, java.util.Date postedOnDate,
			InputStream ebBillInputStream, List<InputStream> propertyImagesInputStreamList) {

		String insertPropertyDetailsQuery = "INSERT INTO property_details (property_type, sqft, furnishing, available_from, rent, address, posted_on_date, EB_Bill, owner_id, location) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		jdbcTemplate.update(insertPropertyDetailsQuery, propertyType, sqft, furnishing,
				new java.sql.Date(availableFrom.getTime()), rentPrice, address,
				new java.sql.Date(postedOnDate.getTime()), ebBillInputStream, userId, location);

		Integer propertyId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

		String insertPropertyImagesQuery = "INSERT INTO property_images (images, property_id) VALUES (?, ?)";
		for (InputStream propertyImageInputStream : propertyImagesInputStreamList) {
			jdbcTemplate.update(insertPropertyImagesQuery, new Object[] { propertyImageInputStream, propertyId },
					new int[] { Types.BLOB, Types.INTEGER });
		}
	}

	@Override
	public void subscription(Users user) {
		try {
			String query = "INSERT INTO property_subscription (owner_id, subscription_duration, payment_amount, payment_status) VALUES (?, ?, ?, ?)";

			jdbcTemplate.update(query, user.getUserid(), 90, 200, true);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	@Override
	public List<SellerPropertyForm> getPropertyDetails() {
		String query = "SELECT * FROM property_details where is_approval=false";

		return jdbcTemplate.query(query, new SellerPropertyFormMapper());
	}

	@Override
	public List<PropertyImage> getPropertyImage(int propertyId) {
	    String sql = "SELECT * FROM property_images WHERE property_id = ?";
	    return jdbcTemplate.query(sql, new Object[]{propertyId}, (rs, rowNum) -> {
	        PropertyImage image = new PropertyImage();
	        image.setImageId(rs.getInt("image_id"));
	        image.setImage(rs.getBytes("images"));
	        image.setPropertyId(rs.getInt("property_id"));
	        return image;
	    });
	}

	@Override
	public void approveproperty(int propertyid) {
		String query = "update property_details set is_approval =true  where property_id =?";
		jdbcTemplate.update(query, propertyid);

	}

	@Override
	public List<SellerPropertyForm> getApprovedProperties() {
		String query = "SELECT * FROM property_details WHERE is_approval = true and rent_id  IS NULL";
		return jdbcTemplate.query(query, new SellerPropertyFormMapper());
	}

	@Override
	public void comment(int userid, String comment, int propertyid) {
		String query="insert into comments (user_id ,comment_section,property_id)values(?,?,?)";
		jdbcTemplate.update(query,userid ,comment,propertyid);
		
	}

	@Override
	public List<Comment> getcomment(int propertyid) {
		
		String query="select c.user_id as user_id,c.comment_section, u.user_name as user_name,c.property_id from comments as c join users u on c.user_id=u.user_id where property_id=?";
		return jdbcTemplate.query(query, new CommentMapper(), propertyid);
	}

	@Override
	public List<SellerPropertyForm> searchApprovedProperties(String location, int budget) {
		String query = "SELECT * FROM property_details WHERE is_approval = true AND location = ? AND rent <= ?";
		return jdbcTemplate.query(query,new SellerPropertyFormMapper(),location,budget);
		
	}

	@Override
	public String owneremailid(int sellerid) {
		String query="select email from users where user_id=? ";
		   String email = jdbcTemplate.queryForObject(query, String.class, sellerid);
	        return email;
	    	}

	@Override
	public void buyerrequest(int ownersid, int buyersid, int propertesid) {
		String query="insert into request(owner_id,rent_id,property_id) values(?,?,?)";
		jdbcTemplate.update(query, ownersid,buyersid,propertesid);
		
	}

	@Override
	public List<SellerDashBoardRequest> sellerdashboard(int buyersid) {
		 String query = "SELECT " +
	                "o.user_id AS owner_id, " +
	                "o.user_name AS owner_name, " +
	                "o.email AS owner_email, " +
	                "o.phonenumber AS owner_phonenumber, " +
	                "r.user_id AS renter_id, " +
	                "r.user_name AS renter_name, " +
	                "r.email AS renter_email, " +
	                "r.phonenumber AS renter_phonenumber, " +
	                "pd.property_id, " +
	                "pd.property_type, " +
	                "pd.sqft, " +
	                "pd.furnishing, " +
	                "pd.available_from, " +
	                "pd.rent, " +
	                "pd.address, " +
	                "pd.posted_on_date, " +
	                "pd.EB_Bill " +
	                "FROM request req " +
	                "JOIN users o ON req.owner_id = o.user_id " +
	                "JOIN users r ON req.rent_id = r.user_id " +
	                "JOIN property_details pd ON req.property_id = pd.property_id " +
	                "WHERE req.owner_id = ? and approval=0";
		return jdbcTemplate.query(query, new SellerDashBoardRequestMapper(),buyersid);
	}

	@Override
	public Users checkseller(Users user) {
	    String sql = "SELECT * FROM users WHERE user_id = ?";
	    return jdbcTemplate.queryForObject(sql, new Object[]{user.getUserid()}, new UserMapper());
		
		
	}

	@Override
	public void sellermail(int propertyId) {
		String query= "update request set approval =true  where property_id =?";
	    jdbcTemplate.update(query, propertyId);
		
	}

	@Override
	public void buyer(int buyerid, int propertyid) {
		String query = "update  property_details  set rent_id=?  where property_id=?";
		 jdbcTemplate.update(query,buyerid,propertyid);
		
	}

	
	@Override
	public List<SellerDashBoard> sellerdashboardrequest(int buyersid) {
		 String query = "SELECT * FROM request WHERE owner_id = ?";
		return jdbcTemplate.query(query, new SellerDashBoardMapper(),buyersid);
	}



}
