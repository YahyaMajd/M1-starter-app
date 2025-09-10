"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const hobby_controller_1 = require("./hobby.controller");
const router = (0, express_1.Router)();
const hobbyController = new hobby_controller_1.HobbyController();
router.get('/', hobbyController.getAllHobbies);
exports.default = router;
