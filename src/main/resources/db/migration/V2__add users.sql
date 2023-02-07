INSERT INTO `users`(`email`, `password`, `role`) VALUES ('user@gmail.com', '$2a$10$DAJw87bOrV.8HFxUgO2qsulbnqZA2le6PpixvgQDWSXPChy6YaTme', 'USER');
INSERT INTO `users`(`email`, `password`, `role`) VALUES ('user2@gmail.com', '$2a$10$SV3oTxolhiSWsuckNfP83u6U4y0jwRFzb/DY9BjtIxqdfqg33jRye', 'USER');

INSERT IGNORE `products`(`available`, `price`, `title`) VALUES (100, 300, 'book');
INSERT IGNORE `products`(`available`, `price`, `title`) VALUES (30, 2000, 'phone');
INSERT IGNORE `products`(`available`, `price`, `title`) VALUES (1000, 10, 'pen');
INSERT IGNORE `products`(`available`, `price`, `title`) VALUES (10, 5000, 'toilet');