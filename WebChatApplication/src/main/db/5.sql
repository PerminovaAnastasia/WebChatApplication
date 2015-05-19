Select count(text), user_id From messages join users on messages.user_id = users.id 
group by user_id Having count(text)>3