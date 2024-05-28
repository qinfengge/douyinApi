import {
	baseUrl
} from './config';



export function randomList() {
	return uni.request({
		url: `${baseUrl}/randomList`,
		method: 'GET'
	});
}

export function random() {
	return uni.request({
		url: `${baseUrl}/random`,
		method: 'GET'
	});
}

export function exRandom(data) {
	return uni.request({
		url: `${baseUrl}/exRandom`,
		method: 'POST',
		data:data
	});
}

export function findById(id) {
	return uni.request({
		url: `${baseUrl}/findById/${id}`,
		method: 'POST',
	});
}