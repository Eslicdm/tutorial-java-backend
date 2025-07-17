INSERT INTO member(name, age, owner, deleted_date) VALUES ('esli', 30, 'esli', NULL);
INSERT INTO member(name, age, owner, deleted_date) VALUES ('alice', 25, 'esli', '2024-12-31 23:59:59');
INSERT INTO member(name, age, owner, deleted_date) VALUES ('bob', 40, 'bill', NULL);

INSERT INTO member_sons(member_id, sons) VALUES (1, 'Lucas');
INSERT INTO member_sons(member_id, sons) VALUES (1, 'Ana');
INSERT INTO member_sons(member_id, sons) VALUES (3, 'Eva');