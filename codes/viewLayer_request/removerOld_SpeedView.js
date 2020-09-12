use twitter
show collections
db.getCollection('speedView').deleteMany({date_debut: 
	{
		$lt: new Date(new Date().setDate(new Date().getDate()-1))
	}
})





db.getCollection('speedView').find({date_debut: {$lt: ISODate("2020-09-05T09:00:34.657Z")}})
db.getCollection('speedView').deleteMany({date_debut: {$lt: ISODate("2020-09-05T09:00:34.657Z")}})